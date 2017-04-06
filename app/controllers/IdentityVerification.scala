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

import auth.GGAction
import config.{ApplicationConfig, VPLSessionCache}
import connectors._
import connectors.identityVerificationProxy.IdentityVerificationProxyConnector
import models.{IVDetails, IndividualAccount, PersonalDetails}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class IdentityVerification(groups: GroupAccounts,
                           individuals: IndividualAccounts,
                           auth: VPLAuthConnector,
                           ggAction: GGAction,
                           keystore: VPLSessionCache,
                           identityVerification: IdentityVerificationConnector,
                           addresses: Addresses,
                           identityVerificationProxyConnector: IdentityVerificationProxyConnector,
                           val appConfig: ApplicationConfig,
                           val messagesApi: MessagesApi
                          ) extends PropertyLinkingController {

  def startIv = ggAction.async { _ => implicit request =>
    if (appConfig.ivEnabled) {
      keystore.getPersonalDetails flatMap { d =>
        identityVerificationProxyConnector.start(appConfig.applicationBaseUrl + routes.IdentityVerification.restoreSession().url,
          appConfig.applicationBaseUrl + routes.IdentityVerification.fail().url, d.ivDetails, None).map(l => Redirect(l.link))
      }
    } else {
      Future.successful(Redirect(routes.IdentityVerification.success()).addingToSession("journeyId" -> java.util.UUID.randomUUID().toString))
    }
  }

  def fail = Action { implicit request =>
    Ok(views.html.identityVerification.failed())
  }

  def restoreSession = Action.async { implicit request =>
    Redirect(routes.IdentityVerification.success).addingToSession(
      SessionKeys.authToken -> request.session.get("bearerToken").getOrElse(""),
      SessionKeys.sessionId -> request.session.get("oldSessionId").getOrElse("")
    )
  }

  def success = ggAction.async { implicit ctx => implicit request =>
    request.session.get("journeyId").fold(Future.successful(Unauthorized("Unauthorised"))) { journeyId =>
      identityVerification.verifySuccess(journeyId) flatMap {
        case true => continue
        case false => Unauthorized("Unauthorised")
      }
    }
  }

  private def continue(implicit ctx: AuthContext, request: Request[_]) = {
    request.session.get("journeyId").fold(Future.successful(Unauthorized("Unauthorised"))) { journeyId =>
      val eventualGroupId = auth.getGroupId(ctx)
      val eventualExternalId = auth.getExternalId(ctx)
      val eventualIndividualDetails = keystore.getPersonalDetails

      for {
        groupId <- eventualGroupId
        userId <- eventualExternalId
        account <- groups.withGroupId(groupId)
        details <- eventualIndividualDetails
        id <- registerAddress(details)
        d = details.withAddressId(id)
        res <- account match {
          case Some(acc) => individuals.create(IndividualAccount(userId, journeyId, acc.id, d.individualDetails)) map { _ =>
            Ok(views.html.createAccount.groupAlreadyExists(acc.companyName))
          }
          case _ => keystore.cachePersonalDetails(d) map { _ => Ok(views.html.identityVerification.success()) }
        }
      } yield res
    }
  }

  private def registerAddress(details: PersonalDetails)(implicit hc: HeaderCarrier): Future[Int] = details.address.addressUnitId match {
    case Some(id) => id
    case None => addresses.create(details.address)
  }
}

case class StartIVVM(data: IVDetails, url: String)
