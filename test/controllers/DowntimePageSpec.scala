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

///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.propertyLinking
//
//import config.ApplicationConfig
//import controllers.{ControllerSpec, DowntimePage}
//import org.mockito.ArgumentMatchers.{eq => matching}
//import org.scalatest.FlatSpec
//import org.scalatestplus.play.OneAppPerTest
//import play.api.test.FakeRequest
//import utils._
//
//class DowntimePageSpec extends ControllerSpec {
//
////  class Setup extends ControllerSpec {
//    lazy val request = FakeRequest().withSession(token)
//    object TestDowntimePageController extends DowntimePage(app.injector.instanceOf[ApplicationConfig])
////  }
//
//  "The downtime page" should "be accessible if the downtimePageEnabled flag is set to true" in  {
//    override val additionalAppConfig = Seq("featureFlags.searchSortEnabled" -> "true")
//    val res = TestDowntimePageController.plannedImprovements()(request)
//    val content = HtmlPage(res)
//    content.mustContainText("Planned improvements")
//  }
//
//  "The downtime page" should "not be accessible if the downtimePageEnabled flag is set to false" in new ControllerSpec {
//    override val additionalAppConfig = Seq("featureFlags.searchSortEnabled" -> "false")
//    val res = TestDowntimePageController.plannedImprovements()(request)
//    val content = HtmlPage(res)
//    content.mustContainText("Not found")
//  }
//
//}
