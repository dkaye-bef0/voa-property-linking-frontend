/*
 * Copyright 2016 HM Revenue & Customs
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

package session

import config.Wiring
import config.ImplicitLifting._
import controllers.Account
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Results.{BadRequest, Forbidden}
import play.api.mvc._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

case class LinkingSessionRequest[A](ses: LinkingSession, account: Account, request: Request[A]) extends WrappedRequest[A](request) {
  def sessionId: String = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session)).sessionId.map(_.value).getOrElse(throw NoSessionId)
}

case object NoSessionId extends Exception

object WithLinkingSession extends ActionBuilder[LinkingSessionRequest] with ActionRefiner[Request, LinkingSessionRequest] {
  def hc(request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
  val repo = Wiring().sessionRepository
  val accountRepo = Wiring().accountConnector

  override protected def refine[A](request: Request[A]) = {
    repo.get()(hc(request)) flatMap {
      case Some(x) => {
        val accId = request.session.get("accountId")
          .getOrElse(throw new Exception("No Account ID"))
        accountRepo.get(accId)(hc(request)).map(account => account.map(acc =>
          Right(LinkingSessionRequest(x, acc, request)))
          .getOrElse(Left(BadRequest(s"Invalid Session")))
        )
      }
      case None => Future.successful(Left(BadRequest(s"Invalid Session")))
    }
  }
}

case class AuthenticatedRequest[A](account: Account, request: Request[A]) extends WrappedRequest[A](request)

object WithAuthentication extends ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] {
  val accountRepo = Wiring().accountConnector

  def hc(request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

  override protected def refine[A](request: Request[A]) =
    request.session.get("accountId") match {
      case Some(aid) => {
        accountRepo.get(aid)(hc(request)).map( account => account.map( acc =>
          Right(AuthenticatedRequest(acc, request))
        ).getOrElse(Left(Forbidden)))
      }
      case None => Left(Forbidden)
    }
}