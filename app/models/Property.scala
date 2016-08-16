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

package models

import connectors.PrototypeTestData
import connectors.ServiceContract.CapacityDeclaration

case class Address(lines: Seq[String], postcode: String) {
  override def toString = (lines ++ Seq(postcode)) mkString ", "
}

case class Property(uarn: String, billingAuthorityReference: String, address: Address, isSelfCertifiable: Boolean, canReceiveMail: Boolean)

object IsAlreadyLinked {
  def apply(property: Property, declaration: CapacityDeclaration) =
    property == PrototypeTestData.conflictedProperty
}

trait NamedEnum {
  def name:String
  def key:String
  def msgKey: String = s"$key.$name"
}

trait NamedEnumSupport[E <: NamedEnum]{

  def all:List[E]

  def fromName(name: String): Option[E] = {
    all.find { _.name.equalsIgnoreCase(name) }
  }

  def options = all.map(_.name)
}