package helpers.graphql

import java.util.UUID

import graphql.codegen.AddConsignment.{addConsignment => ac}
import graphql.codegen.GetSeries.{getSeries => gs}
import graphql.codegen.AddTransferAgreement.{AddTransferAgreement => ata}
import graphql.codegen.AddFiles.{addFiles => af}
import graphql.codegen.AddAntivirusMetadata.{AddAntivirusMetadata => aav}
import graphql.codegen.types.{AddAntivirusMetadataInput, AddConsignmentInput, AddFilesInput, AddTransferAgreementInput}
import helpers.keycloak.UserCredentials
import uk.gov.nationalarchives.tdr.GraphQlResponse

class GraphqlUtility(userCredentials: UserCredentials) {

  def createConsignment(body: String): Option[ac.Data] = {
    val client = GraphqlClient[ac.Data, ac.Variables](userCredentials)
    val seriesId = getSeries(body).get.getSeries.head.seriesid
    client.result(ac.document, ac.Variables(AddConsignmentInput(seriesId))).data
  }

  def getSeries(body: String): Option[gs.Data] = {
    val client = GraphqlClient[gs.Data, gs.Variables](userCredentials)
    client.result(gs.document, gs.Variables(body)).data
  }

  def createTransferAgreement(consignmentId: UUID): Unit = {
    val client = GraphqlClient[ata.Data, ata.Variables](userCredentials)
    val input = AddTransferAgreementInput(consignmentId, Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))
    client.result(ata.document, ata.Variables(input))
  }

  def createFiles(consignmentId: UUID): List[UUID] = {
    val client = GraphqlClient[af.Data, af.Variables](userCredentials)
    val input = AddFilesInput(consignmentId, 4)
    client.result(af.document, af.Variables(input)).data.get.addFiles.fileIds
  }

  def createAVMetadata(fileId: UUID): GraphQlResponse[aav.Data] = {
    val client = new BackendApiClient[aav.Data, aav.Variables]
    val input = AddAntivirusMetadataInput(fileId, "E2E tests software", "E2E tests software version", "E2E test DB version", "E2E test result", System.currentTimeMillis)
    client.sendRequest(aav.document, aav.Variables(input))
  }

}

object GraphqlUtility {
  def apply(userCredentials: UserCredentials): GraphqlUtility = new GraphqlUtility(userCredentials)
}
