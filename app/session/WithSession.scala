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
import play.api.mvc.{ActionBuilder, ActionRefiner, Request, WrappedRequest}
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.mvc.Results.BadRequest
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class LinkingSessionRequest[A](ses: LinkingSession, request: Request[A]) extends WrappedRequest[A](request)

object WithLinkingSession extends ActionBuilder[LinkingSessionRequest] with ActionRefiner[Request, LinkingSessionRequest] {
  val repo = Wiring().sessionRepository

  implicit def hc(request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

  override protected def refine[A](request: Request[A]) =
    repo.get()(hc(request)) map {
      case Some(x) => Right(LinkingSessionRequest(x, request))
      case None => Left(BadRequest(s"Invalid Session"))
    }

}
