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

import config.Wiring
import play.api.data.Form
import play.api.data.Forms._
import form.Mappings._

trait ChooseEvidence extends PropertyLinkingController {

  val linkingSessionRepo = Wiring().sessionRepository
  val withLinkingSession = Wiring().withLinkingSession

  def show = withLinkingSession { implicit request =>
    request.ses.hasRatesBill match {
      case Some(h) => Ok(views.html.uploadRatesBill.chooseEvidence(form.fill(h)))
      case _ => Ok(views.html.uploadRatesBill.chooseEvidence(form))
    }
  }

  def submit = withLinkingSession { implicit request =>
    form.bindFromRequest().fold(
      errors => BadRequest(views.html.uploadRatesBill.chooseEvidence(errors)),
      hasRatesBill => linkingSessionRepo.saveOrUpdate(request.ses.copy(hasRatesBill = Some(hasRatesBill))) map { _ =>
        if (hasRatesBill) {
          Redirect(routes.UploadRatesBill.show)
        } else {
          Redirect(routes.UploadEvidence.show)
        }
      }
    )
  }

  lazy val form = Form(single(keys.hasRatesBill -> mandatoryBoolean))

  lazy val keys = new {
    val hasRatesBill = "hasRatesBill"
  }
}

object ChooseEvidence extends ChooseEvidence
