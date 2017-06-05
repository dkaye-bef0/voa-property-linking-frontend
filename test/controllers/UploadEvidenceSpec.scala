/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.io.File

import connectors.EnvelopeConnector
import connectors.fileUpload.{EnvelopeMetadata, FileUploadConnector}
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{eq => matching, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import repositories.SessionRepo
import resources._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadEvidenceSpec extends ControllerSpec with FileUploadTestHelpers {
  implicit val request = FakeRequest().withSession(token)

  val wsClient = app.injector.instanceOf[WSClient]
  val envConnectorStub = new EnvelopeConnector(wsClient) {
    override def closeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String] =  {
      Future.successful(envelopeId)
    }
    override def storeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String] =  {
      Future.successful(envelopeId)
    }
  }

  val withLinkingSession = new StubWithLinkingSession(mockSessionRepo)
  object TestUploadEvidence extends UploadEvidence(mockFileUploads, envConnectorStub, mockSessionRepo, withLinkingSession)  {
    override val propertyLinks = StubPropertyLinkConnector
  }

  lazy val mockSessionRepo = {
    val f = mock[SessionRepo]
    when(f.start(any())(any(), any())
    ).thenReturn(Future.successful(()))
    when(f.saveOrUpdate(any())(any(), any())
    ).thenReturn(Future.successful(()))
    f
  }

  lazy val mockFileUploads = {
    val m = mock[FileUploadConnector]
    when(m.createEnvelope(any[EnvelopeMetadata])(any[HeaderCarrier])).thenReturn(Future.successful(envelopeId))
    when(m.uploadFile(matching(envelopeId), anyString, anyString, any[File])(any[HeaderCarrier])).thenReturn(Future.successful(()))
    m
  }

  lazy val envelopeId: String = shortString

  "Upload Evidence page" must "contain a file input" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val res = TestUploadEvidence.show()(request)
    status(res) mustBe OK
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))
    page.mustContainFileInput("evidence")
  }

  it must "have its form submit targetted to the file upload service" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val res = TestUploadEvidence.show()(request)
    status(res) mustBe OK
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))
    page.mustContainPartialLink("form", "http.*/file-upload/upload/envelopes/[^/]*/files/[^/]*".r)
  }

  it must "have a redirect-success-url set as part of the form submit's query string - to the evidence-submitted page" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val res = TestUploadEvidence.show()(request)
    status(res) mustBe OK
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))
    page.mustContainPartialLink("form", "redirect-success-url=http.*/property-linking/evidence-submitted".r)
  }

  it must "have a redirect-error-url url set as part of the form submit's query string - to the form page itself" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val res = TestUploadEvidence.show()(request)
    status(res) mustBe OK
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))
    page.mustContainPartialLink("form", "redirect-error-url=http.*/property-linking/upload-evidence".r)
  }

  it must "show an error if the user does not upload any evidence - when errorCode=400 is set" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val req = FakeRequest(Helpers.GET, "/property-linking/upload-evidence?errorCode=400")

    val res = TestUploadEvidence.show(errorCode = Some(400))(req)
    status(res) mustBe BAD_REQUEST
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))

    page.mustContainSummaryErrors(("evidence", "Please upload evidence so that we can verify your link to the property", "Please upload some evidence"))
    page.mustContainFieldErrors(("evidence_", "Please upload some evidence"))
  }

  it must "show an error if the user uploads a file greater than 10MB - when errorCode=413 is set" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val req = FakeRequest(Helpers.GET, "/property-linking/upload-evidence?errorCode=413")

    val res = TestUploadEvidence.show(errorCode = Some(413))(req)
    status(res) mustBe BAD_REQUEST
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))

    page.mustContainSummaryErrors(("evidence", "Please upload evidence so that we can verify your link to the property", "File size must be less than 10MB"))
    page.mustContainFieldErrors(("evidence_", "File size must be less than 10MB"))
  }

  it must "show an error if the user uploads a file that is not a JPG or PDF - when errorCode=415 is set" in {
    withLinkingSession.stubSession(arbitrary[LinkingSession], arbitrary[DetailedIndividualAccount], arbitrary[GroupAccount])

    val req = FakeRequest(Helpers.GET, "/property-linking/upload-evidence?errorCode=415")

    val res = TestUploadEvidence.show(errorCode = Some(415))(req)
    status(res) mustBe BAD_REQUEST
    val page = HtmlPage(Jsoup.parse(contentAsString(res)))

    page.mustContainSummaryErrors(("evidence", "Please upload evidence so that we can verify your link to the property", "File must be a PDF or JPG"))
    page.mustContainFieldErrors(("evidence_", "File must be a PDF or JPG"))
  }
}
