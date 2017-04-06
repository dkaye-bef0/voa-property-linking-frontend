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

package config

import akka.stream.Materializer
import com.kenshoo.play.metrics._
import com.softwaremill.macwire._
import connectors.ConnectorsModule
import controllers.{Assets, ControllersModule}
import org.slf4j.MDC
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.{DefaultHttpErrorHandler, DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.core.SourceMapper
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter
import play.twirl.api.Html
import uk.gov.hmrc.play.audit.EventTypes.{ResourceNotFound, ServerInternalError, ServerValidationError, TransactionFailureReason}
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.audit.http.HttpAuditEvent
import uk.gov.hmrc.play.audit.http.config.{AuditingConfig, BaseUri, Consumer, LoadAuditingConfig}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.inject.{AppName, DefaultAppName, DefaultServicesConfig}
import uk.gov.hmrc.play.filters.frontend.{CSRFExceptionsFilter, DeviceIdFilter, SessionTimeoutFilter}
import uk.gov.hmrc.play.frontend.bootstrap.FrontendFilters
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter
import uk.gov.hmrc.play.http.{JsValidationException, NotFoundException}

import scala.concurrent.Future

class VPLApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = new VPLComponents(context).application
}

class VPLComponents(context: Context) extends BuiltInComponentsFromContext(context) with ConnectorsModule with ControllersModule {
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }

  lazy val servicesConfig = wire[DefaultServicesConfig]
  lazy val applicationConfig = wire[ApplicationConfig]

  lazy val metrics = wire[MetricsImpl]
  lazy val metricsController = wire[MetricsController]
  lazy val assets = wire[Assets]

  lazy val healthRoutes = {
    val prefix = "/"
    wire[health.Routes]
  }

  lazy val templateRoutes = {
    val prefix = "/template"
    wire[template.Routes]
  }

  lazy val appRoutes = {
    val prefix = "/"
    wire[app.Routes]
  }

  override val router: Router = {
    val prefix = "/business-rates-property-linking"
    wire[prod.Routes]
  }
}

trait MDTPComponents {
  self: BuiltInComponents =>

  lazy val appName = wire[DefaultAppName]

  Logger.info(s"Starting microservice : ${appName.appName} : in mode : ${environment.mode}")

  lazy val auditConnector = wire[AuditServiceConnector]

  if (environment.mode != Mode.Test) {
    wire[Graphite].onStart(application)
  }
}

class VPLHttpRequestHandler(router: Router,
                            httpErrorHandler: HttpErrorHandler,
                            httpConfiguration: HttpConfiguration,
                            httpFilters: Seq[EssentialFilter]
                           ) extends DefaultHttpRequestHandler(router, httpErrorHandler, httpConfiguration, httpFilters: _*) {
  override def routeRequest(request: RequestHeader): Option[Handler] = {
    router.handlerFor(request).orElse {
      Some(request.path).filter(_.endsWith("/")).flatMap(p => router.handlerFor(request.copy(path = p.dropRight(1))))
    }
  }
}

class VPLErrorHandling(val auditConnector: AuditServiceConnector,
                       val appName: String,
                       environment: Environment,
                       configuration: Configuration,
                       appConfig: ApplicationConfig,
                       messages: MessagesApi,
                       sourceMapper: Option[SourceMapper] = None,
                       router: => Option[Router] = None
                      ) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router) with ErrorAuditing {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    implicit val messagesApi = messages
    implicit val appConf = appConfig
    views.html.errors.error(pageTitle, heading, message)
  }
}


//Because ErrorAuditingSettings depends on GlobalSettings
trait ErrorAuditing extends HttpAuditEvent {
  self: DefaultHttpErrorHandler =>

  def auditConnector: AuditConnector

  private val unexpectedError = "Unexpected error"
  private val notFoundError = "Resource Endpoint Not Found"
  private val badRequestError = "Request bad format exception"

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val code = exception match {
      case e: NotFoundException => ResourceNotFound
      case jsError: JsValidationException => ServerValidationError
      case _ => ServerInternalError
    }

    auditConnector.sendEvent(dataEvent(code, unexpectedError, request)
      .withDetail((TransactionFailureReason, exception.getMessage)))

    Future.successful(InternalServerError(standardErrorTemplate(
      Messages("global.error.InternalServerError500.title"),
      Messages("global.error.InternalServerError500.heading"),
      Messages("global.error.InternalServerError500.message")
    )))
  }

  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    auditConnector.sendEvent(dataEvent(ServerValidationError, badRequestError, request))

    Future.successful(BadRequest(standardErrorTemplate(
      Messages("global.error.badRequest400.title"),
      Messages("global.error.badRequest400.heading"),
      Messages("global.error.badRequest400.message")
    )))
  }

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    auditConnector.sendEvent(dataEvent(ResourceNotFound, notFoundError, request))

    Future.successful(NotFound(standardErrorTemplate(
      Messages("global.error.pageNotFound404.title"),
      Messages("global.error.pageNotFound404.heading"),
      Messages("global.error.pageNotFound404.message")
    )))
  }

  override protected def onForbidden(request: RequestHeader, message: String): Future[Result] = self.onForbidden(request, message)

  override protected def onOtherClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = self.onOtherClientError(request, statusCode, message)

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html
}

class Graphite(configuration: Configuration) extends GraphiteConfig {
  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = configuration.getConfig("metrics")
}

class VPLFilters(configuration: Configuration,
                 metrics: Metrics,
                 appName: String,
                 val csrfFilter: CSRFFilter,
                 val csrfExceptionsFilter: CSRFExceptionsFilter,
                 val metricsFilter: MetricsFilter,
                 val deviceIdFilter: DeviceIdFilter,
                 val frontendAuditFilter: FrontendAuditFilter,
                 val loggingFilter: FrontendLoggingFilter,
                 val securityFilter: SecurityHeadersFilter,
                 val sessionTimeoutFilter: SessionTimeoutFilter)(implicit materializer: Materializer) extends FrontendFilters {

  lazy val enableSecurityHeaderFilter = configuration.getBoolean("security.headers.filter.enabled").getOrElse(true)

  def filters = if (enableSecurityHeaderFilter) Seq(securityFilter) ++ frontendFilters  else frontendFilters
}

class AuditServiceConnector(configuration: Configuration) extends AuditConnector {
  override lazy val auditingConfig: AuditingConfig = loadAuditingConfig("auditing")

  private def loadAuditingConfig(key: String) = {
    configuration.getConfig(key).map { c =>

      val enabled = c.getBoolean("enabled").getOrElse(true)

      if(enabled) {
        AuditingConfig(
          enabled = enabled,
          traceRequests = c.getBoolean("traceRequests").getOrElse(true),
          consumer = Some(c.getConfig("consumer").map { con =>
            Consumer(
              baseUri = con.getConfig("baseUri").map { uri =>
                BaseUri(
                  host = uri.getString("host").getOrElse(throw new Exception("Missing consumer host for auditing")),
                  port = uri.getInt("port").getOrElse(throw new Exception("Missing consumer port for auditing")),
                  protocol = uri.getString("protocol").getOrElse("http")
                )
              }.getOrElse(throw new Exception("Missing consumer baseUri for auditing"))
            )
          }.getOrElse(throw new Exception("Missing consumer configuration for auditing")))
        )
      } else {
        AuditingConfig(consumer = None, enabled = false, traceRequests = false)
      }

    }
  }.getOrElse(throw new Exception("Missing auditing configuration"))
}