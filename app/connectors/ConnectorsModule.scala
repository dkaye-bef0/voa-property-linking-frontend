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

import auth.{GGAction, GovernmentGatewayProvider}
import com.softwaremill.macwire._
import config.{ApplicationConfig, VPLHttp}
import connectors.fileUpload.FileUploadConnector
import connectors.identityVerificationProxy.IdentityVerificationProxyConnector
import connectors.propertyLinking.PropertyLinkConnector
import play.api.libs.ws.ahc.AhcWSComponents
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp

@Module
trait ConnectorsModule extends AhcWSComponents {

  lazy val ggAction = wire[GGAction]
  lazy val ggProvider = wire[GovernmentGatewayProvider]

  lazy val fileUploadConnector = wire[FileUploadConnector]
  lazy val envelopeConnector = wire[EnvelopeConnector]
  lazy val ivProxyConnector = wire[IdentityVerificationProxyConnector]
  lazy val propertyLinkingConnector = wire[PropertyLinkConnector]
  lazy val addresses = wire[Addresses]
  lazy val authorisationConnector = wire[AuthorisationConnector]
  lazy val businessRatesAuthorisation = wire[BusinessRatesAuthorisation]
  lazy val businessRatesValuationConnector = wire[BusinessRatesValuationConnector]
  lazy val dvrCaseManagementConnector = wire[DVRCaseManagementConnector]
  lazy val groupAccounts = wire[GroupAccounts]
  lazy val identityVerificationConnector = wire[IdentityVerificationConnector]
  lazy val individualAccounts = wire[IndividualAccounts]
  lazy val propertyRepresentationConnector = wire[PropertyRepresentationConnector]
  lazy val submissionIdConnector = wire[SubmissionIdConnector]
  lazy val trafficThrottleConnector = wire[TrafficThrottleConnector]
  lazy val vplAuthConnector = wire[VPLAuthConnector]

  def http: WSHttp = new VPLHttp
  def applicationConfig: ApplicationConfig
  def servicesConfig: ServicesConfig
}
