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

import config.ApplicationConfig
import play.api.i18n.I18nSupport
import play.api.mvc.{Controller, Request}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait PropertyLinkingController extends Controller with I18nSupport {
  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
  implicit def future[A](a: A): Future[A] = Future.successful(a)
  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val appConfig: ApplicationConfig
  implicit val config = appConfig
}
