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

package models

import serialization.EnumFormat

sealed trait RepresentationStatus extends NamedEnum {
  val name: String
  val key = "propReprStatus"
  override def toString = name
}

case object RepresentationAccepted extends RepresentationStatus {
  val name = "ACCEPTED"
}

case object RepresentationPending extends RepresentationStatus {
  val name = "PENDING"
}


object RepresentationStatus extends NamedEnumSupport[RepresentationStatus] {
  implicit val format = EnumFormat(RepresentationStatus)

  override def all: List[RepresentationStatus] = List(RepresentationAccepted, RepresentationPending)
}