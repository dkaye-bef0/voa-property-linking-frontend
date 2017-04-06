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

package config

import org.joda.time.LocalDate
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel

class ApplicationConfig(config: Configuration, environment: Environment) {

  def applicationBaseUrl = if (environment.mode == Mode.Prod) "" else "http://localhost:9523"
  val contact = getConfig("contact-frontend.url")
  val contactFormServiceIdentifier = "CCA"

  val vmvUrl = getConfig("vmv-frontend.url")
  val sivUrl = getConfig("identity-verification-frontend.url")

  val ggSignInUrl = getConfig("gg-sign-in.url")
  val ggRegistrationUrl = getConfig("gg-registration.url")
  val ggContinueUrl = applicationBaseUrl + controllers.routes.Dashboard.home().url
  val betaLoginRequired = getConfig("featureFlags.betaLoginRequired").toBoolean
  val ivEnabled = getConfig("featureFlags.ivEnabled").toBoolean
  val ivConfidenceLevel = ConfidenceLevel.L200

  def businessRatesValuationUrl(page: String) = getConfig("business-rates-valuation.url") + s"/$page"
  val agentEnabled = getConfig("featureFlags.agentsEnabled").toBoolean
  val casesEnabled = getConfig("featureFlags.casesEnabled").toBoolean
  val propertyLinkingEnabled = getConfig("featureFlags.propertyLinkingEnabled").toBoolean

  val propertyLinkingDateThreshold = config.getString("propertyLinkingDateThreshold").fold(new LocalDate(2017,4,1))(LocalDate.parse(_))

  val showReleaseNotes = getConfig("featureFlags.showReleaseNotes").toBoolean

  val allowedMimeTypes: Seq[String] = getConfig("allowedFileUploadTypes").split(",")

  lazy val reportAProblemPartialUrl = s"$contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val analyticsToken = getConfig("google-analytics.token")
  lazy val analyticsHost = getConfig("google-analytics.host")
  lazy val voaPersonID = getConfig("google-analytics.dimensions.voaPersonId")

  private def getConfig(key: String) = config.getString(key).getOrElse(throw ConfigMissing(key))
}

private case class ConfigMissing(key: String) extends Exception(s"Missing config for $key")
