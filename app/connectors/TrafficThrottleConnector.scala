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


import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

import scala.concurrent.Future

class TrafficThrottleConnector(http: HttpGet with HttpPost, servicesConfig: ServicesConfig) {
  val trafficRouter = "voa-traffic-throttle"

  lazy val serviceUrl = s"${servicesConfig.baseUrl(trafficRouter)}/$trafficRouter"

  def isThrottled(route: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$serviceUrl/cca/$route/throttled"

    http.GET[Boolean](url)
  }

}
