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

package auth

import javax.inject.Inject

import config.ApplicationConfig
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext, GovernmentGateway}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals, ~}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GGAction @Inject()(val provider: GovernmentGatewayProvider, val authConnector: uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector) extends Actions with xxx {
  type x = AuthContext
  private def authenticatedBy = AuthenticatedBy(provider, GGConfidence)

//  override def apply(body: (AuthContext) => (Request[AnyContent]) => Result): Unit = authenticatedBy(body)
  override def async(body: (AuthContext) => (Request[AnyContent]) => Future[Result]): Action[AnyContent] = authenticatedBy.async(body)
}

class GGActionEnrolment @Inject()(val authConnector: uk.gov.hmrc.auth.core.AuthConnector, enrolmentHelper: EnrolmentHelper) extends AuthorisedFunctions with xxx {
  type x = UserDetails
  private val retrievals: Retrieval[~[~[Option[String], Option[String]], Option[String]]] = Retrievals.email and Retrievals.externalId and Retrievals.groupIdentifier //Sort this out to what we need.
  private val predicate = AuthProviders(AuthProvider.GovernmentGateway) and Enrolment("HMRC-VOA-CCA")

  override def async(body: (UserDetails) => (Request[AnyContent]) => Future[Result]): Action[AnyContent] = Action.async { implicit request =>
    authorised(predicate)
      .retrieve(retrievals)(email => body(f(email))(request))
      .recoverWith(enrolmentHelper.handleError)
  }

  private def f(x: ~[~[Option[String], Option[String]], Option[String]]): UserDetails = x match {
    case email ~ Some(externalID) ~ Some(groupId) => UserDetails(email, externalID, groupId)
  }

//  override def apply(body: (UserDetails) => (Request[AnyContent]) => Result): Action[AnyContent] = ???
}

class EnrolmentHelper @Inject()(provider: GovernmentGatewayProvider, userDetailsConnector: ) {

  def handleError(implicit request: Request[AnyContent]) : PartialFunction[Throwable, Future[Result]] = {
    case _: InsufficientEnrolments =>
      //Get User details.
      //Check moderized for info,
      //potential create personId,
      //upload enrolment stuff to GG,
      //allow user through.
      Future.successful(Redirect(""))
    case _: NoActiveSession => provider.redirectToLogin
    case otherException =>
      Logger.debug(s"expection thrown on authorization with message : ${otherException.getMessage}")
      throw otherException
  }


}

trait xxx {
  type x

  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

  //  def apply(body: A => Request[AnyContent] => Result): Action[AnyContent]

  def async(body: x => Request[AnyContent] => Future[Result]): Action[AnyContent]
}

class GovernmentGatewayProvider @Inject()(config: ApplicationConfig) extends GovernmentGateway {
  this: ServicesConfig =>
  override def additionalLoginParameters: Map[String, Seq[String]] = Map("accountType" -> Seq("organisation"))
  override def loginURL: String = config.ggSignInUrl
  override def continueURL = config.ggContinueUrl

  override def redirectToLogin(implicit request: Request[_]) = {
    Future.successful(Redirect(loginURL, Map("continue" -> Seq(config.baseUrl + request.uri), "origin" -> Seq("voa")) ++ additionalLoginParameters))
  }
}

case class UserDetails(email: Option[String], externalId: String, groupId: String)
