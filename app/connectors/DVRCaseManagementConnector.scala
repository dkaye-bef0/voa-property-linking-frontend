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

import javax.inject.Inject

import models.DetailedValuationRequest
import uk.gov.hmrc.play.config.inject.ServicesConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import config.WSHttp
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

class DVRCaseManagementConnector @Inject()(config: ServicesConfig, http: WSHttp) {
  val url = config.baseUrl("property-linking") + "/property-linking"

  def requestDetailedValuation(dvr: DetailedValuationRequest)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[DetailedValuationRequest, HttpResponse](url + "/request-detailed-valuation", dvr) map { _ => () }
  }
}
