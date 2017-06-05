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

import java.util.UUID
import javax.inject.{Inject, Named}

import config.{ApplicationConfig, Wiring}
import connectors.EnvelopeConnector
import connectors.fileUpload.FileUploadConnector
import form.EnumMapping
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Call
import repositories.SessionRepo
import session.WithLinkingSession
import views.helpers.Errors

class UploadEvidence @Inject()(override val fileUploader: FileUploadConnector,
                               override val envelopeConnector: EnvelopeConnector,
                               @Named("propertyLinkingSession") override val sessionRepository: SessionRepo,
                               override val withLinkingSession: WithLinkingSession)
  extends PropertyLinkingController with FileUploadHelpers {
  override val propertyLinks = Wiring().propertyLinkConnector

  def show(errorCode: Option[Int] = None, errorMessage: Option[String] = None) = withLinkingSession { implicit request =>
    errorCode match {
      case None => Ok(views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form, request.ses.envelopeId)))
      case Some(413) => BadRequest(
        views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", "error.fileUpload.tooLarge"), request.ses.envelopeId)))
      case Some(415) => BadRequest(
        views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", "error.fileUpload.invalidFileType"), request.ses.envelopeId)))
      case Some(_) => BadRequest(views.html.uploadEvidence.show(
        UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", Errors.missingFiles), request.ses.envelopeId)))
    }
  }

  def noEvidenceUploaded() = withLinkingSession { implicit request =>
    sessionRepository.saveOrUpdate[LinkingSession](request.ses.withLinkBasis(NoEvidenceFlag, None)) map { _ =>
      Redirect(propertyLinking.routes.Declaration.show())
    }
  }
}

object UploadEvidence {
  lazy val form = Form(single("evidenceType" -> EnumMapping(EvidenceType)))
}

case class UploadedEvidence(hasEvidence: HasEvidence, evidenceType: EvidenceType)

case class UploadEvidenceVM(form: Form[_], envelopeId: String, fileId: String = UUID.randomUUID().toString) {
  private val fileUploadBase = ApplicationConfig.fileUploadFrontendUrl
  private val propertyLinkingBase = ApplicationConfig.propertyLinkingUrl

  val fileUploadCall: Call = Call(method = "GET", url = s"$fileUploadBase/upload/envelopes/$envelopeId/files/$fileId" +
    s"?redirect-success-url=$propertyLinkingBase/evidence-submitted" +
    s"&redirect-error-url=$propertyLinkingBase/upload-evidence")
}
