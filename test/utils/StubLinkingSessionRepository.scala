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

package utils

import session.{LinkingSession, LinkingSessionRepository}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class StubLinkingSessionRepository extends LinkingSessionRepository(StubSessionCache) {
  private var stubbedLinkingSession: Option[LinkingSession] = None

  override def start(address: String, uarn: Long, envelopeId: String, submissionId: String, personId: Long)(implicit hc: HeaderCarrier): Future[Unit] = {
    Future.successful {
      stubbedLinkingSession = Some(LinkingSession(address, uarn, envelopeId, submissionId, personId))
    }
  }

  override def saveOrUpdate(session: LinkingSession)(implicit hc: HeaderCarrier): Future[Unit] = {
    Future.successful {
      stubbedLinkingSession = Some(session)
    }
  }

  override def get()(implicit hc: HeaderCarrier): Future[Option[LinkingSession]] = {
    Future.successful(stubbedLinkingSession)
  }

  override def remove()(implicit hc: HeaderCarrier): Future[Unit] = {
    Future.successful {
      stubbedLinkingSession = None
    }
  }
}
