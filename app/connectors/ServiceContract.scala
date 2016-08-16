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

import models.CapacityType
import org.joda.time.DateTime

object ServiceContract {
  case class LinkToProperty(capacityDeclaration: CapacityDeclaration)
  case class CapacityDeclaration(capacity: CapacityType, fromDate: DateTime, toDate: Option[DateTime] = None)

  case class PropertyLink(name: String, uarn: String, billingAuthorityReference: String, capacity: String, linkedDate: DateTime, assessmentYears: Seq[Int])
  case class PendingPropertyLink(name: String, uarn: String, billingAuthorityReference: String, capacity: String, linkedDate: DateTime)
  case class LinkedProperties(added: Seq[PropertyLink], pending: Seq[PendingPropertyLink])
  case class PropertyRepresentation(representationId: String, agentId: String, userId: String, uarn: String,
                                    canCheck: Boolean, canChallenge: Boolean, pending: Boolean)

}