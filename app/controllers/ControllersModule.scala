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

import actions.AuthenticatedAction
import com.softwaremill.macwire._
import config.VPLSessionCache
import connectors.ConnectorsModule
import controllers.propertyLinking.Declaration
import play.api.i18n.I18nComponents
import session.{AgentAppointmentSessionRepository, LinkingSessionRepository, WithLinkingSession}

trait ControllersModule extends I18nComponents {
  self: ConnectorsModule =>

  lazy val vplApplication = wire[Application]
  lazy val login = wire[Login]
  lazy val register = wire[Register]
  lazy val wizard = wire[Wizard]
  lazy val representations = wire[agent.RepresentationController]
  lazy val dashboard = wire[Dashboard]
  lazy val declaration = wire[Declaration]
  lazy val claimProperty = wire[ClaimProperty]
  lazy val uploadEvidence = wire[UploadEvidence]
  lazy val uploadRatesBill = wire[UploadRatesBill]
  lazy val addressLookup = wire[AddressLookup]
  lazy val appointAgentController = wire[AppointAgentController]
  lazy val assessments = wire[Assessments]
  lazy val chooseEvidence = wire[ChooseEvidence]
  lazy val createGroupAccount = wire[CreateGroupAccount]
  lazy val createIndividualAccount = wire[CreateIndividualAccount]
  lazy val identityVerification = wire[IdentityVerification]

  lazy val cache = wire[VPLSessionCache]
  lazy val linkingSessionRepo = wire[LinkingSessionRepository]
  lazy val agentAppointmentSessionRepository = wire[AgentAppointmentSessionRepository]
  lazy val withLinkingSession = wire[WithLinkingSession]
  lazy val authenticatedAction = wire[AuthenticatedAction]
}
