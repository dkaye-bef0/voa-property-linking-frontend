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

import models.{Address, Property}
import serialization.JsonFormats._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.{ExecutionContext, Future}

class PropertyConnector(http: HttpGet)(implicit ec: ExecutionContext) extends ServicesConfig {
  lazy val baseUrl: String = baseUrl("property-representations") + s"/property-linking"

  def find(uarn: String)(implicit hc: HeaderCarrier): Future[Option[Property]] =
    http.GET[Option[Property]](baseUrl + s"/properties/$uarn")
        .map(
          _.orElse(PrototypeTestData.pretendSearchResults.find(_.uarn == uarn)))
        .recover {
          case _ => PrototypeTestData.pretendSearchResults.find(_.uarn == uarn) }
}

object PrototypeTestData {
  lazy val selfCertProperty = Property( "uarn1",
    "testselfcertifiableshop", Address(Seq("1 The Self-cert non-bank street", "The Town"), "AA11 1AA"), true, true
  )
  lazy val conflictedProperty = Property( "uarn2",
    "testconflict", Address(Seq("22 Conflict Self-cert", "The Town"), "AA11 1AA"), true, true
  )
  lazy val bankForRatesBillVerifiedJourney = Property( "uarn3",
    "testbankaccepted", Address(Seq("Banky McBankface (rates bill accepted)", "Some Road", "Some Town"), "AA11 1AA"), false, true
  )
  lazy val bankForRatesBillFailedJourney = Property( "uarn4",
    "testbankrejected", Address(Seq("Banky McSadface (rates bill rejected)", "Some Road", "Some Town"), "AA11 1AA"), false, true
  )
  lazy val pretendSearchResults = Seq(
    selfCertProperty,
    conflictedProperty,
    bankForRatesBillVerifiedJourney,
    bankForRatesBillFailedJourney,
    Property(
      "uarn5", "testbanknomail", Address(Seq("Banky McNoMailFace (Cannot receive mail)", "Some Road", "Some Town"), "AA11 1AA"), false, false
    )
  )

  def canBeLinkedTo(baRef: String) =
    baRef == bankForRatesBillVerifiedJourney.billingAuthorityReference ||
    baRef == selfCertProperty.billingAuthorityReference

}
