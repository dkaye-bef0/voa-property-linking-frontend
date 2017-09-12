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

package controllers.manageDetails

import actions.{AuthenticatedAction, BasicAuthenticatedRequest}
import auth.GovernmentGatewayProvider
import javax.inject.Inject
import config.{ApplicationConfig, Global}
import connectors.BusinessRatesAuthorisation
import play.api.mvc.{Action, AnyContent, Result}
import play.api.mvc.Results._

import scala.concurrent.Future

class EditDetailsAction @Inject()(config: ApplicationConfig,
                                  provider: GovernmentGatewayProvider,
                                  businessRatesAuthorisation: BusinessRatesAuthorisation)
  extends AuthenticatedAction(provider, businessRatesAuthorisation) {

  override def apply(body: (BasicAuthenticatedRequest[AnyContent]) => Future[Result]): Action[AnyContent] = {
    if (config.editDetailsEnabled) { super.apply(body) } else { notFound }
  }

  private def notFound = Action { implicit request =>
    NotFound(Global.notFoundTemplate)
  }
}
