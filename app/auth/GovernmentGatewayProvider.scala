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
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext, GovernmentGateway}
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

class GGAction @Inject()(val provider: GovernmentGatewayProvider, val authConnector: AuthConnector) extends Actions {
  private def authenticatedBy = AuthenticatedBy(provider, GGConfidence)

  def apply(body: AuthContext => Request[AnyContent] => Result) = authenticatedBy(body)
  def async(body: AuthContext => Request[AnyContent] => Future[Result]) = authenticatedBy.async(body)
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
