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

package controllers.propertyLinking

import config.ApplicationConfig
import connectors.propertyLinking.PropertyLinkConnector
import connectors.{CapacityDeclaration, EnvelopeConnector, FileInfo}
import controllers.PropertyLinkingController
import form.Mappings._
import models.{LinkBasis, NoEvidenceFlag}
import play.api.data.{Form, FormError, Forms}
import play.api.i18n.MessagesApi
import session.{LinkingSessionRepository, LinkingSessionRequest, WithLinkingSession}
import views.html.propertyLinking.declaration

class Declaration(envelopes: EnvelopeConnector,
                  linkingSessionRepo: LinkingSessionRepository,
                  withLinkingSession: WithLinkingSession,
                  propertyLinks: PropertyLinkConnector,
                  val appConfig: ApplicationConfig,
                  val messagesApi: MessagesApi) extends PropertyLinkingController {

  def show = withLinkingSession { implicit request =>
    request.ses.linkBasis match {
      case Some(basis) => Ok(declaration(DeclarationVM(request.ses.address, request.ses.declaration, request.ses.fileInfo, form)))
      case None => Unauthorized
    }
  }

  def submit = withLinkingSession { implicit request =>
    val session = request.ses

    session.linkBasis match {
      case Some(basis) =>
        form.bindFromRequest().value match {
          case Some(true) => submitLinkingRequest(basis) map { _ => showConfirmation(basis) }
          case _ => BadRequest(declaration(DeclarationVM(session.address, session.declaration, session.fileInfo, formWithNoDeclaration)))
        }
      case None => Unauthorized
    }
  }

  private def showConfirmation(basis: LinkBasis)(implicit request: LinkingSessionRequest[_]) = basis match {
    case NoEvidenceFlag => Redirect(routes.Declaration.noEvidence().url)
    case _ => Redirect(routes.Declaration.confirmation())
  }

  def confirmation = withLinkingSession { implicit request =>
    linkingSessionRepo.remove() map { _ =>
      Ok(views.html.linkingRequestSubmitted(RequestSubmittedVM(request.ses.address, request.ses.submissionId)))
    }
  }

  def noEvidence = withLinkingSession { implicit request =>
    linkingSessionRepo.remove() map { _ => Ok(views.html.uploadEvidence.noEvidenceUploaded(RequestSubmittedVM(request.ses.address, request.ses.submissionId))) }
  }

  private def submitLinkingRequest(basis: LinkBasis)(implicit request: LinkingSessionRequest[_]) = {
    val session = request.ses
    for {
      _ <- propertyLinks.linkToProperty(basis)
      _ <- envelopes.closeEnvelope(session.envelopeId)
    } yield ()
  }

  lazy val form = Form(Forms.single("declaration" -> mandatoryBoolean))
  lazy val formWithNoDeclaration = form.withError(FormError("declaration", "declaration.required"))
}

case class DeclarationVM(address: String, declaration: CapacityDeclaration, fileInfo: Option[FileInfo], form: Form[_])

case class RequestSubmittedVM(address: String, refId: String)