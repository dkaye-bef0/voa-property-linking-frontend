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

package controllers

import auth.GGAction
import config.VPLSessionCache
import connectors._
import form.Mappings._
import form.TextMatching
import models.{Address, IndividualAccount}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.http.HeaderCarrier
import views.helpers.Errors

import scala.concurrent.Future

class CreateGroupAccount(groups: GroupAccounts,
                         individuals: IndividualAccounts,
                         auth: VPLAuthConnector,
                         ggAction: GGAction,
                         keystore: VPLSessionCache,
                         identityVerification: IdentityVerificationConnector,
                         addresses: Addresses,
                         val messagesApi: MessagesApi
                        ) extends PropertyLinkingController {

  import CreateGroupAccount._

  def show = ggAction.async { _ =>
    implicit request =>
      request.session.get("journeyId").fold(Future.successful(Unauthorized("Unauthorised"))) { journeyId =>
        identityVerification.verifySuccess(journeyId) flatMap {
          case true => Ok(views.html.createAccount.group(form))
          case false => Unauthorized("Unauthorised")
        }
      }
  }

  def success = ggAction.async { _ =>
    implicit request =>
      request.session.get("journeyId").fold(Future.successful(Unauthorized("Unauthorised"))) { journeyId =>
        identityVerification.verifySuccess(journeyId) flatMap {
          case true => Ok(views.html.createAccount.confirmation())
          case false => Unauthorized("Unauthorised")
        }
      }
  }

  def submit = ggAction.async { ctx =>
    implicit request =>
      request.session.get("journeyId").fold(Future.successful(Unauthorized("Unauthorised"))) { journeyId =>
        identityVerification.verifySuccess(journeyId) flatMap {
          case true =>
            form.bindFromRequest().fold(
              errors => BadRequest(views.html.createAccount.group(errors)),
              formData => {
                val eventualGroupId = auth.getGroupId(ctx)
                val eventualExternalId = auth.getExternalId(ctx)
                val eventualPersonalDetails = keystore.getPersonalDetails
                val addressId = registerAddress(formData)

                for {
                  groupId <- eventualGroupId
                  userId <- eventualExternalId
                  details <- eventualPersonalDetails
                  id <- addressId
                  organisationId <- groups.create(groupId, id, formData)
                  _ <- individuals.create(IndividualAccount(userId, journeyId, organisationId, details.individualDetails))
                } yield {
                  Redirect(routes.CreateGroupAccount.success())
                }
              }
            )
          case false => Unauthorized("Unauthorised")
        }
      }
  }

  private def registerAddress(details: GroupAccountDetails)(implicit hc: HeaderCarrier): Future[Int] = details.address.addressUnitId match {
    case Some(id) => Future.successful(id)
    case None => addresses.create(details.address)
  }

  lazy val form = Form(mapping(
    keys.companyName -> nonEmptyText(maxLength = 45),
    keys.address -> address,
    keys.email -> email.verifying(Constraints.maxLength(45)),
    keys.confirmEmail -> TextMatching(keys.email, Errors.emailsMustMatch),
    keys.phone -> nonEmptyText(maxLength = 20),
    keys.isSmallBusiness -> mandatoryBoolean,
    keys.isAgent -> mandatoryBoolean
  )(GroupAccountDetails.apply)(GroupAccountDetails.unapply))

  implicit def vm(form: Form[_]): CreateGroupAccountVM = CreateGroupAccountVM(form)
}

object CreateGroupAccount {
  val keys = new {
    val companyName = "companyName"
    val address = "address"
    val email = "businessEmail"
    val confirmEmail = "confirmedBusinessEmail"
    val phone = "businessPhone"
    val isSmallBusiness = "isSmallBusiness"
    val isAgent = "isAgent"
  }
}

case class CreateGroupAccountVM(form: Form[_])

case class GroupAccountDetails(companyName: String, address: Address, email: String, confirmedEmail: String,
                               phone: String, isSmallBusiness: Boolean, isAgent: Boolean)
