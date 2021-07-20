package helpers.graphql

import java.time.Instant
import java.util.UUID
import graphql.codegen.AddAntivirusMetadata.{AddAntivirusMetadata => aav}
import graphql.codegen.AddFilesAndMetadata.{AddFilesAndMetadata => afam}
import graphql.codegen.StartUpload.{StartUpload => su}
import graphql.codegen.AddConsignment.{addConsignment => ac}
import graphql.codegen.AddFileMetadata.{addFileMetadata => afm}
import graphql.codegen.AddFFIDMetadata.{addFFIDMetadata => affm}
import graphql.codegen.AddFilesAndMetadata.AddFilesAndMetadata.AddFilesAndMetadata
import graphql.codegen.AddTransferAgreement.{AddTransferAgreement => ata}
import graphql.codegen.GetSeries.{getSeries => gs}
import graphql.codegen.GetConsignmentExport.{getConsignmentForExport => gcfe}
import graphql.codegen.types._
import helpers.graphql.GraphqlUtility.MatchIdInfo
import helpers.keycloak.UserCredentials

import java.nio.file.Path

class GraphqlUtility(userCredentials: UserCredentials) {

  def createConsignment(body: String): Option[ac.Data] = {
    val client = new UserApiClient[ac.Data, ac.Variables](userCredentials)
    val seriesId: UUID = getSeries(body).get.getSeries.head.seriesid
    client.result(ac.document, ac.Variables(AddConsignmentInput(seriesId))).data
  }

  def getSeries(body: String): Option[gs.Data] = {
    val client = new UserApiClient[gs.Data, gs.Variables](userCredentials)
    client.result(gs.document, gs.Variables(body)).data
  }

  def createTransferAgreement(consignmentId: UUID): Unit = {
    val client = new UserApiClient[ata.Data, ata.Variables](userCredentials)
    val input = AddTransferAgreementInput(consignmentId, true, true, true, true, true, true)
    client.result(ata.document, ata.Variables(input))
  }

  def addFilesAndMetadata(consignmentId: UUID, parentFolderName: String, matchIdInfo: List[MatchIdInfo]): List[afam.AddFilesAndMetadata] = {
    val startUploadClient = new UserApiClient[su.Data, su.Variables](userCredentials)
    startUploadClient.result(su.document, su.Variables(StartUploadInput(consignmentId, parentFolderName)))
    val client = new UserApiClient[afam.Data, afam.Variables](userCredentials)

    val metadataInput = matchIdInfo.map(info =>
      ClientSideMetadataInput(
        s"E2E_tests/original/path${info.matchId}",
        info.checksum,
        Instant.now().toEpochMilli,
        1024,
        info.matchId
      )
    )
    val input = AddFileAndMetadataInput(consignmentId, metadataInput, isComplete = true)
    client.result(afam.document, afam.Variables(input)).data.get.addFilesAndMetadata
  }

  def createAVMetadata(fileId: UUID, result: String = ""): Unit = {
    val client = new BackendApiClient[aav.Data, aav.Variables]
    val input = AddAntivirusMetadataInput(fileId, "E2E tests software", "E2E tests software version", "E2E test DB version", result, System.currentTimeMillis)
    client.sendRequest(aav.document, aav.Variables(input))
  }

  def createBackendChecksumMetadata(fileId: UUID, checksumValue: Option[String]): Unit = {
    val client = new BackendApiClient[afm.Data, afm.Variables]
    val input = AddFileMetadataInput("SHA256ServerSideChecksum", fileId, checksumValue.getOrElse("checksumValue"))
    client.sendRequest(afm.document, afm.Variables(input))
  }

  def createFfidMetadata(fileId: UUID, puid: String = "x-fmt/111"): Unit = {

    val client = new BackendApiClient[affm.Data, affm.Variables]
    val ffidInputMatches = FFIDMetadataInputMatches(Some("txt"), "e2e-test-basis", Some(puid))
    val input = FFIDMetadataInput(
      fileId,
      "e2e-test-software",
      "e2e-test-software-version",
      "e2e-test-binary-signature-file",
      "e2e-test-container-signature.xml",
      "e2e-test-method",
      List(ffidInputMatches))
    client.sendRequest(affm.document, affm.Variables(input))
  }

  def getConsignmentExport(consignmentId: UUID): Option[gcfe.Data] = {
    val client = new UserApiClient[gcfe.Data, gcfe.Variables](userCredentials)
    client.result(gcfe.document, gcfe.Variables(consignmentId)).data
  }
}

object GraphqlUtility {
  case class MatchIdInfo(checksum: String, path: Path, matchId: Int)
  def apply(userCredentials: UserCredentials): GraphqlUtility = new GraphqlUtility(userCredentials)
}
