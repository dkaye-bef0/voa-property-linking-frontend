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

import config.ApplicationConfig
import connectors.fileUpload.FileUploadConnector
import connectors.propertyLinking.PropertyLinkConnector
import connectors.{EnvelopeConnector, FileInfo}
import form.EnumMapping
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import session.{LinkingSessionRepository, WithLinkingSession}
import views.helpers.Errors

class UploadEvidence(val propertyLinks: PropertyLinkConnector,
                     val withLinkingSession: WithLinkingSession,
                     val linkingSession: LinkingSessionRepository,
                     val fileUploader: FileUploadConnector,
                     val envelopeConnector: EnvelopeConnector,
                     val messagesApi: MessagesApi,
                     val appConfig: ApplicationConfig
                    ) extends PropertyLinkingController with FileUploadHelpers {

  def show() = withLinkingSession { implicit request =>
    Ok(views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form)))
  }

  def submit() = withLinkingSession { implicit request =>
    UploadEvidence.form.bindFromRequest().fold(
      error => BadRequest(views.html.uploadEvidence.show(UploadEvidenceVM(error))),
      uploaded => {
        val filePart = request.request.body.asMultipartFormData.get.file("evidence[]").flatMap(x => if (x.filename.isEmpty) None else Some(x))
        uploadIfNeeded(filePart) flatMap { x =>
          x match {
            case FileAccepted =>
              val fileInfo = FileInfo(filePart.fold("no file")(_.filename), uploaded.name)
              linkingSession.saveOrUpdate(request.ses.withLinkBasis(OtherEvidenceFlag, Some(fileInfo))) map { _ =>
                Redirect(propertyLinking.routes.Declaration.show)
              }
            case FileTooLarge => BadRequest(
              views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", "error.fileUpload.tooLarge")))
            )
            case InvalidFileType => BadRequest(
              views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", "error.fileUpload.invalidFileType")))
            )
            case FileMissing => BadRequest(views.html.uploadEvidence.show(
              UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", Errors.missingFiles))))
          }
        }
      }
    )
  }

  def noEvidenceUploaded() = withLinkingSession { implicit request =>
    linkingSession.saveOrUpdate(request.ses.withLinkBasis(NoEvidenceFlag, None)) map { _ =>
      Redirect(propertyLinking.routes.Declaration.show())
    }
  }
}

object UploadEvidence {
  lazy val form = Form(single("evidenceType" -> EnumMapping(EvidenceType)))
}

case class UploadedEvidence(hasEvidence: HasEvidence, evidenceType: EvidenceType)

case class UploadEvidenceVM(form: Form[_])
