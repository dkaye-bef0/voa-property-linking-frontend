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

package connectors

import java.io.File
import javax.inject.Inject

import akka.stream.scaladsl._
import com.google.inject.{ImplementedBy, Singleton}
import config.{Environment, Wiring}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HttpException, HttpResponse, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[EnvelopeConnector])
trait Envelope {
  def storeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String]
}

@Singleton
class EnvelopeConnector @Inject()(val ws: WSClient)(implicit ec: ExecutionContext) extends Envelope with ServicesConfig with JsonHttpReads {
  lazy val http = Wiring().http

  def storeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[String] = {
    http.PUT[JsValue, HttpResponse](s"${baseUrl("property-linking")}/property-linking/envelopes/${envelopeId}", Json.obj()) map { _ => envelopeId
    }
  }

}
