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

import config.VPLSessionCache
import connectors.Authenticated
import models._
import org.jsoup.Jsoup
import org.scalacheck.Arbitrary.arbitrary
import play.api.Logger
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.api.test.Helpers._
import resources._
import utils._
import scala.collection.JavaConverters._

class AppointAgentSpec extends ControllerSpec {

  private object TestAppointAgent extends AppointAgentController {
    lazy val sessionCache = new VPLSessionCache(StubHttp)
    override val representations = StubPropertyRepresentationConnector
    override val accounts = StubGroupAccountConnector
    override val propertyLinks = StubPropertyLinkConnector
    override val authenticated = StubAuthentication
    override val sessionRepository = new StubAgentAppointmentSessionRepository(sessionCache)
  }

  val request = FakeRequest().withSession(token)

  "The appoint a new agent page" must "allow the user to enter the agent code, and set permissions for checks and challenges" in {
    stubLoggedInUser()

    val res = TestAppointAgent.appoint(arbitrary[PropertyLink].sample.get.authorisationId)(request)
    status(res) must be (OK)

    val page = HtmlPage(res)
    page.mustContainTextInput("#agentCode")
    page.mustContainRadioSelect("canCheck", AgentPermission.options)
    page.mustContainRadioSelect("canChallenge", AgentPermission.options)
  }

  it must "require the agent code to be numeric" in {
    stubLoggedInUser()

    val res = TestAppointAgent.appointSubmit(arbitrary[Long])(
      request.withFormUrlEncodedBody("agentCode" -> "agents inc", "canCheck" -> StartAndContinue.name, "canChallenge" -> StartAndContinue.name)
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)
    page.mustContainFieldErrors("agentCode" -> "This must be a number")
  }

  it must "trim leading and trailing spaces from the agent code" in {
    val (groupAccount, individual) = stubLoggedInUser()
    StubGroupAccountConnector.stubAccount(arbitrary[GroupAccount].sample.get)
    val link = arbitrary[PropertyLink].sample.get.copy(organisationId = groupAccount.id, authorisationId = 555, agents = Nil)
    StubPropertyLinkConnector.stubLink(link)
    StubPropertyRepresentationConnector.stubAgentCode(123)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> " 123 ", "canCheck" -> StartAndContinue.name, "canChallenge" -> StartAndContinue.name)
    )
    status(res) must be (SEE_OTHER)
    redirectLocation(res) mustBe Some(routes.AppointAgentController.appointed(link.authorisationId).url)
  }

  it must "require the user to enter an agent code" in {
    stubLoggedInUser()
    val groupAccount = arbitrary[GroupAccount].sample.get

    StubGroupAccountConnector.stubAccount(groupAccount)
    val link = arbitrary[PropertyLink].sample.get.copy(organisationId = groupAccount.id)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> "", "canCheck" -> StartAndContinue.name, "canChallenge" -> StartAndContinue.name)
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)
    page.mustContainFieldErrors("agentCode" -> "This must be filled in")
  }

  it must "require the user to select agent permissions for checks" in {
    stubLoggedInUser()
    val agentAccount = arbitrary[GroupAccount].sample.get
    StubGroupAccountConnector.stubAccount(agentAccount)

    val res = TestAppointAgent.appointSubmit(arbitrary[PropertyLink].sample.get.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> agentAccount.agentCode.toString, "canCheck" -> "", "canChallenge" -> StartAndContinue.name)
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)

    page.mustContainFieldErrors("canCheck" -> "Please select an option")

  }

  it must "require the user to select agent permissions for challenges" in {
    stubLoggedInUser()
    val agentAccount = arbitrary[GroupAccount].sample.get
    StubGroupAccountConnector.stubAccount(agentAccount)

    val res = TestAppointAgent.appointSubmit(arbitrary[PropertyLink].sample.get.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> agentAccount.agentCode.toString, "canCheck" -> StartAndContinue.name, "canChallenge" -> "")
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)

    page.mustContainFieldErrors("canChallenge" -> "Please select an option")

  }

  it must "not allow agents to be appointed with no permissions" in {
    val (groupAccount, individualAccount) = stubLoggedInUser()
    val agentAccount = arbitrary[GroupAccount].sample.get
    StubGroupAccountConnector.stubAccount(agentAccount)
    val link:PropertyLink = arbitrary[PropertyLink].sample.get.copy(organisationId = groupAccount.id)
    StubPropertyLinkConnector.stubLink(link)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> agentAccount.agentCode.toString, "canCheck" -> NotPermitted.name, "canChallenge" -> NotPermitted.name)
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)
    page.mustContainFieldErrors("canCheck" -> "Agent must either have permission to continue checks or challenges")
    page.mustContainFieldErrors("canChallenge" -> "Agent must either have permission to continue checks or challenges")
  }

  it must "require the agent code to be valid" in {
    val (groupAccount, _) = stubLoggedInUser()
    val link = arbitrary[PropertyLink].sample.get.copy(organisationId = groupAccount.id)
    StubPropertyLinkConnector.stubLink(link)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> "999", "canCheck" -> StartAndContinue.name, "canChallenge" -> NotPermitted.name)
    )
    status(res) must be (BAD_REQUEST)

    val page = HtmlPage(res)
    page.mustContainFieldErrors("agentCode" -> "Invalid agent code")
  }

  it must "not allow an agent to appoint themselves" in {
    val (groupAccount, _) = stubLoggedInUser()
    val link: PropertyLink = arbitrary[PropertyLink]

    StubPropertyLinkConnector.stubLink(link)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> groupAccount.agentCode.toString, "canCheck" -> StartAndContinue.name, "canChallenge" -> NotPermitted.name)
    )
    status(res) mustBe BAD_REQUEST

    HtmlPage(res).mustContainFieldErrors("agentCode" -> "You can’t appoint your own business as your agent")
  }

  it must "display the success page when the form is valid, and no permission have previously been set" in {
    val (groupAccount, individual) = stubLoggedInUser()
    val link = arbitrary[PropertyLink].sample.get.copy(organisationId = groupAccount.id, authorisationId = 555, agents = Nil)
    StubPropertyLinkConnector.stubLink(link)
    StubPropertyRepresentationConnector.stubAgentCode(123)

    val res = TestAppointAgent.appointSubmit(link.authorisationId)(
      request.withFormUrlEncodedBody("agentCode" -> "123", "canCheck" -> StartAndContinue.name, "canChallenge" -> StartAndContinue.name)
    )

    status(res) must be (SEE_OTHER)
    redirectLocation(res) mustBe Some(routes.AppointAgentController.appointed(link.authorisationId).url)
  }

  "The back link on the appoint agents page" must "go to the manage properties page if the user came from the manage properties page" in {
    stubLoggedInUser()

    val res = TestAppointAgent.appoint(arbitrary[PropertyLink].authorisationId)(FakeRequest().withHeaders(HeaderNames.REFERER -> routes.Dashboard.manageProperties().url))
    status(res) mustBe OK

    val html = Jsoup.parse(contentAsString(res))
    html.select("#cancel").attr("href") mustBe routes.Dashboard.manageProperties().url
  }

  it must "go to the properties managed by agent page if the user came from the properties managed by agent page" in {
    stubLoggedInUser()
    val agentCode: Long = positiveLong
    val authorisationId = arbitrary[PropertyLink].authorisationId
    val request = FakeRequest("GET", routes.AppointAgentController.appoint(authorisationId).url).withHeaders(HeaderNames.REFERER -> routes.Dashboard.viewManagedProperties(agentCode).url)

    val res = TestAppointAgent.appoint(arbitrary[PropertyLink].authorisationId)(request)
    status(res) mustBe OK

    val html = Jsoup.parse(contentAsString(res))
    html.select("#cancel").attr("href") mustBe routes.Dashboard.viewManagedProperties(agentCode).url
  }

  //filling in the form incorrectly changes the referer to be the same as the page url
  it must "go to the manage properties page if the user has filled in the form incorrectly" in {
    stubLoggedInUser()
    val agentCode: Long = positiveLong
    val authorisationId = arbitrary[PropertyLink].authorisationId

    val request = FakeRequest("GET", routes.AppointAgentController.appoint(authorisationId).url).withHeaders(HeaderNames.REFERER -> routes.AppointAgentController.appoint(authorisationId).url)
    val res = TestAppointAgent.appoint(authorisationId)(request)
    status(res) mustBe OK

    val html = Jsoup.parse(contentAsString(res))
    html.select("#cancel").attr("href") mustBe routes.Dashboard.manageProperties.url
  }

  private def stubLoggedInUser() = {
    val groupAccount = groupAccountGen.sample.get
    val individual = individualGen.sample.get
    StubGroupAccountConnector.stubAccount(groupAccount)
    StubIndividualAccountConnector.stubAccount(individual)
    StubAuthentication.stubAuthenticationResult(Authenticated(Accounts(groupAccount, individual)))
    (groupAccount, individual)
  }
}
