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

package connectors

import java.io.Serializable
import javax.inject.Inject

import auth.UserDetails
import config.WSHttp
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class VPLAuthConnector @Inject()(serverConfig: ServicesConfig, val http: WSHttp) extends AuthConnector {
  override val serviceUrl: String = serverConfig.baseUrl("auth")

  def getExternalId[A](ctx: A)(implicit hc: HeaderCarrier): Future[String] = ctx match {
    case x: AuthContext => getExternalId(x)
    case y: UserDetails => getExternalId(y)
    case _ => throw new IllegalArgumentException //Useful expection.
  }

  def getExternalId(ctx: AuthContext)(implicit hc: HeaderCarrier): Future[String] = getIds[JsValue](ctx) map { r =>
    (r \ "externalId").as[String]
  }

  def getExternalId(userDetails: UserDetails): Future[String] =
    Future.successful(userDetails.externalId)

  def getGroupId[A](ctx: A)(implicit hc: HeaderCarrier): Future[String] = ctx match {
    case x: AuthContext => getGroupId(x)
    case y: UserDetails => getGroupId(y)
    case _ => throw new IllegalArgumentException
  }

  private def getGroupId(authContext: AuthContext)(implicit hc: HeaderCarrier): Future[String] = getUserDetails[JsValue](authContext) map { r =>
    (r \ "groupIdentifier").as[String]
  }

  private def getGroupId(userDetails: UserDetails): Future[String] =
    Future.successful(userDetails.groupId)
}

class UserDetialsConnector
