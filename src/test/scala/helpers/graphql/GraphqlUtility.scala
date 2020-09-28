package helpers.graphql

import java.time.Instant
import java.util.UUID

import graphql.codegen.AddAntivirusMetadata.{AddAntivirusMetadata => aav}
import graphql.codegen.AddClientFileMetadata.{AddClientFileMetadata => acf}
import graphql.codegen.AddConsignment.{addConsignment => ac}
import graphql.codegen.AddFileMetadata.{addFileMetadata => afm}
import graphql.codegen.AddFiles.{addFiles => af}
import graphql.codegen.AddTransferAgreement.{AddTransferAgreement => ata}
import graphql.codegen.GetSeries.{getSeries => gs}
import graphql.codegen.types._
import helpers.keycloak.UserCredentials

class GraphqlUtility(userCredentials: UserCredentials) {

  def createConsignment(body: String): Option[ac.Data] = {
    val client = new UserApiClient[ac.Data, ac.Variables](userCredentials)
    val seriesId = getSeries(body).get.getSeries.head.seriesid
    client.result(ac.document, ac.Variables(AddConsignmentInput(seriesId))).data
  }

  def getSeries(body: String): Option[gs.Data] = {
    val client = new UserApiClient[gs.Data, gs.Variables](userCredentials)
    client.result(gs.document, gs.Variables(body)).data
  }

  def createTransferAgreement(consignmentId: UUID): Unit = {
    val client = new UserApiClient[ata.Data, ata.Variables](userCredentials)
    val input = AddTransferAgreementInput(consignmentId, Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))
    client.result(ata.document, ata.Variables(input))
  }

  def createFiles(consignmentId: UUID, numberOfFiles: Int): List[UUID] = {
    val client = new UserApiClient[af.Data, af.Variables](userCredentials)
    val input = AddFilesInput(consignmentId, numberOfFiles)
    client.result(af.document, af.Variables(input)).data.get.addFiles.fileIds
  }

  def createAVMetadata(fileId: UUID): Unit = {
    val client = new BackendApiClient[aav.Data, aav.Variables]
    val input = AddAntivirusMetadataInput(fileId, "E2E tests software", "E2E tests software version", "E2E test DB version", "E2E test result", System.currentTimeMillis)
    client.sendRequest(aav.document, aav.Variables(input))
  }

  def createClientsideMetadata(userCredentials: UserCredentials, fileId: UUID, checksumValue: String): Unit = {
    val client = new UserApiClient[acf.Data, acf.Variables](userCredentials)
    val dummyInstant = Instant.now()
    val input = List(AddClientFileMetadataInput(
      fileId,
      Some("E2E_tests/original/path"),
      Some(checksumValue),
      Some("E2E tests checksumType"),
      dummyInstant.toEpochMilli,
      dummyInstant.toEpochMilli,
      Some(1024),
      dummyInstant.toEpochMilli))
    client.result(acf.document, acf.Variables(input))
  }

  def createBackendChecksumMetadata(fileId: UUID): Unit = {
    val client = new BackendApiClient[afm.Data, afm.Variables]
    val input = AddFileMetadataInput("SHA256ServerSideChecksum", fileId, "checksumValue")
    client.sendRequest(afm.document, afm.Variables(input))
  }
}

object GraphqlUtility {
  def apply(userCredentials: UserCredentials): GraphqlUtility = new GraphqlUtility(userCredentials)
}
