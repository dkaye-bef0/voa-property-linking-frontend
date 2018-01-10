package utils

import connectors.{Addresses, TaxEnrolmentConnector}
import models.Address
import org.mockito.ArgumentMatchers.{eq => matching, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, MustMatchers}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object TestGovernmentGatewayUserService {

}

class TestGovernmentGatewayUserServiceSpec extends FlatSpec with MustMatchers with MockitoSugar with ScalaFutures {
  "GG user service" should "create user under a GG organisation" in {
  }

  "created user" should "not be asked to register when logging in" in {
  }

  "created user" should "be recognised as belonging to the existing organisation" in {
  }
}