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

import config.ApplicationConfig
import form.EnumMapping
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AnyContent, Call, Result}
import repositories.SessionRepo
import session.{LinkingSessionRequest, WithLinkingSession}
import views.helpers.Errors
import views.html._

class UploadEvidence @Inject()(@Named("propertyLinkingSession") val sessionRepository: SessionRepo,
                               val withLinkingSession: WithLinkingSession) extends PropertyLinkingController {

  def show(errorCode: Option[Int] = None, errorMessage: Option[String] = None) = withLinkingSession { implicit request =>
    val envelopeId = request.ses.envelopeId
    errorCode match {
      case None => Ok(uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form, envelopeId)))
      case Some(413) => badRequest("error.fileUpload.tooLarge", envelopeId)
      case Some(415) => badRequest("error.fileUpload.invalidFileType", envelopeId)
      case Some(_) => badRequest(Errors.missingFiles, envelopeId)
    }
  }

  def noEvidenceUploaded() = withLinkingSession { implicit request =>
    sessionRepository.saveOrUpdate[LinkingSession](request.ses.withLinkBasis(NoEvidenceFlag, None)) map { _ =>
      Redirect(propertyLinking.routes.Declaration.show())
    }
  }

  private def badRequest(errorCode: String, envelopeId: String)(implicit request: LinkingSessionRequest[AnyContent]) = {
    BadRequest(uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", errorCode), envelopeId)))
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
    s"?redirect-success-url=$propertyLinkingBase/${propertyLinking.routes.Declaration.show().url}" +
    s"&redirect-error-url=$propertyLinkingBase/upload-evidence")
}
