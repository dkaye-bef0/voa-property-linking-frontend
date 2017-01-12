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

import models.Address
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsDefined, JsNumber, JsValue}

import scala.concurrent.Future

class Addresses(http: HttpGet with HttpPost) extends ServicesConfig {

  val url = baseUrl("property-linking") + "/property-linking/address"

  def findByPostcode(postcode: String)(implicit hc: HeaderCarrier): Future[Seq[Address]] = {
    http.GET[Seq[Address]](url + s"?postcode=$postcode")
  }

  def create(address: Address)(implicit hc: HeaderCarrier): Future[Int] = {
    http.POST[Address, JsValue](url, address) map { js =>
      js \ "id" match {
        case JsDefined(JsNumber(n)) => n.toInt
        case _ => throw new Exception(s"Unexpected response: $js")
      }
    }
  }
}