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

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HttpResponse, _}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

trait Envelope {
  def storeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String]
  def closeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String]
}

class EnvelopeConnector(val ws: WSClient, http: WSHttp, servicesConfig: ServicesConfig) extends Envelope with JsonHttpReads {

  private lazy val url = servicesConfig.baseUrl("property-linking")

  def storeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String] = {
    http.POST[JsValue, HttpResponse](s"$url/property-linking/envelopes/$envelopeId", Json.obj()) map { _ => envelopeId }
  }

  def closeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String] = {
    http.PUT[JsValue, HttpResponse](s"$url/property-linking/envelopes/$envelopeId", Json.obj()) map { _ => envelopeId }
  }

}
