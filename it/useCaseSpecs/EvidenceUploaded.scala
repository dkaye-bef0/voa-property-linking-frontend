package useCaseSpecs

import useCaseSpecs.utils._

class EvidenceUploaded extends FrontendTest {

  import TestData._

  //"Given an interested person has uploaded some evidence for a property" - {
  //  implicit val sid: SessionId = java.util.UUID.randomUUID.toString
  //  implicit val session = GGSession(userId, token)
  //  HTTP.stubKeystoreSession(SessionDocument(nonSelfCertProperty, envelopeId, Some(declaration),
  //    selfCertifyComplete = Some(false)), Seq(Account(userId, false)))
  //  HTTP.stubAuthentication(session)
  //  HTTP.stubGroupId(session, groupId)

  //  "When they arrive at the evidence submitted page" - {
  //    val page = Page.get("/property-linking/evidence-uploaded")

  //    "They see confirmation that their property linking request has been submitted" ignore {
  //      page.mustContainSuccessSummary(s"Thank you for your request which has been submitted to the Valuation Office Agency.")
  //    }

  //    "And the page contains a link to the dashboard" ignore {
  //      page.mustContainLink("#backToDashBoard", "/property-linking/manage-properties")
  //    }
  //  }
  //}

  object TestData {
    lazy val baRef = "sfku03802342"
    lazy val uarn = "uarn03802342"
    lazy val address = Address("leen1", "leen2", "leen3", "AA11 1AA")
    lazy val userId = "389u4asldkjfasljdf"
    lazy val envelopeId = "asdfasfsaf"
    lazy val groupId = "98yqwfib09qy8iuah09"
    lazy val token = "tasfnmsfk"
    lazy val formattedAddress = "leen1, leen2, leen3, AA11 1AA"
    lazy val nonSelfCertProperty = Property(uarn, baRef, address, false, true)
    lazy val declaration = CapacityDeclaration("occupier", "2001-01-01", None)
  }

}