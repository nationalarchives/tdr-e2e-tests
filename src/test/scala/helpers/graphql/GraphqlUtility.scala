package helpers.graphql

import cats.implicits.catsSyntaxOptionId

import java.time.Instant
import java.util.UUID
import graphql.codegen.AddBulkAntivirusMetadata.{addBulkAntivirusMetadata => abavmd}
import graphql.codegen.AddFilesAndMetadata.{addFilesAndMetadata => afam}
import graphql.codegen.StartUpload.{startUpload => su}
import graphql.codegen.AddConsignment.{addConsignment => ac}
import graphql.codegen.AddBulkFileMetadata.{addBulkFileMetadata => abfm}
import graphql.codegen.AddBulkFFIDMetadata.{addBulkFFIDMetadata => abffm}
import graphql.codegen.AddTransferAgreementPrivateBeta.{addTransferAgreementPrivateBeta => atapb}
import graphql.codegen.AddTransferAgreementCompliance.{addTransferAgreementCompliance => atac}
import graphql.codegen.GetCustomMetadata.{customMetadata => cm}
import graphql.codegen.GetDisplayProperties.{displayProperties => dp}
import graphql.codegen.GetSeries.{getSeries => gs}
import graphql.codegen.GetConsignmentExport.{getConsignmentForExport => gcfe}
import graphql.codegen.GetConsignmentSummary.{getConsignmentSummary => gcs}
import graphql.codegen.StartUpload.{startUpload => su}
import graphql.codegen.UpdateConsignmentSeriesId.{updateConsignmentSeriesId => ucs}
import graphql.codegen.UpdateConsignmentStatus.{updateConsignmentStatus => ucst}
import graphql.codegen.AddConsignmentStatus.{addConsignmentStatus => acs}
import graphql.codegen.types._
import graphql.codegen.{AddMultipleFileStatuses => amfs}
import helpers.graphql.GraphqlUtility.MatchIdInfo
import helpers.keycloak.UserCredentials

import java.nio.file.Path

class GraphqlUtility(userCredentials: UserCredentials) {
  val standardConsignmentType = "standard"
  val judgmentConsignmentType = "judgment"

  def createConsignment(consignmentType: String, body: String): Option[ac.Data] = {
    val client = new UserApiClient[ac.Data, ac.Variables](userCredentials)
    if(consignmentType.equals("judgment")) {
      client.result(ac.document, ac.Variables(AddConsignmentInput(None, judgmentConsignmentType))).data
    } else {
      val seriesId: UUID = getSeries(body).get.getSeries.head.seriesid
      client.result(ac.document, ac.Variables(AddConsignmentInput(Some(seriesId), standardConsignmentType))).data
    }
  }

  def startUpload(consignmentId: UUID, parentFolder: String = "parent"): Option[su.Data] = {
    val client = new UserApiClient[su.Data, su.Variables](userCredentials)
    client.result(su.document, su.Variables(StartUploadInput(consignmentId, parentFolder, None))).data
  }

  def getSeries(body: String): Option[gs.Data] = {
    val client = new UserApiClient[gs.Data, gs.Variables](userCredentials)
    client.result(gs.document, gs.Variables(body)).data
  }

  def updateSeries(consignmentId: UUID, body: String): Unit = {
    val client = new UserApiClient[ucs.Data, ucs.Variables](userCredentials)
    val seriesId: UUID = getSeries(body).get.getSeries.head.seriesid
    val input = UpdateConsignmentSeriesIdInput(consignmentId, seriesId)
    client.result(ucs.document, ucs.Variables(input))
  }

  def createTransferAgreementPrivateBeta(consignmentId: UUID): Unit = {
    val client = new UserApiClient[atapb.Data, atapb.Variables](userCredentials)
    val input = AddTransferAgreementPrivateBetaInput(consignmentId, true, true, None)
    client.result(atapb.document, atapb.Variables(input))
  }

  def createTransferAgreementCompliance(consignmentId: UUID): Unit = {
    val client = new UserApiClient[atac.Data, atac.Variables](userCredentials)
    val input = AddTransferAgreementComplianceInput(consignmentId, true, None, true)
    client.result(atac.document, atac.Variables(input))
  }

  def addFilesAndMetadata(consignmentId: UUID, parentFolderName: String, matchIdInfo: List[MatchIdInfo], includeTopLevelFolder: Boolean = false): List[afam.AddFilesAndMetadata] = {
    val startUploadClient = new UserApiClient[su.Data, su.Variables](userCredentials)
    startUploadClient.result(su.document, su.Variables(StartUploadInput(consignmentId, parentFolderName, includeTopLevelFolder.some)))
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
    val input = AddFileAndMetadataInput(consignmentId, metadataInput, None)
    client.result(afam.document, afam.Variables(input)).data.get.addFilesAndMetadata
  }

  def addFileStatus(fileId: UUID, statusType: String, statusValue: String): List[amfs.addMultipleFileStatuses.AddMultipleFileStatuses] = {
    val fileStatusClient = new UserApiClient[amfs.addMultipleFileStatuses.Data, amfs.addMultipleFileStatuses.Variables](userCredentials)
    val variables = amfs.addMultipleFileStatuses.Variables(AddMultipleFileStatusesInput(List(AddFileStatusInput(fileId, statusType, statusValue))))
    fileStatusClient.result(amfs.addMultipleFileStatuses.document, variables).data.get.addMultipleFileStatuses
  }

  def addConsignmentStatus(consignmentId: UUID, statusType: String, statusValue: String): Option[acs.Data] = {
    val addConsignmentStatusClient = new UserApiClient[acs.Data, acs.Variables](userCredentials)
    addConsignmentStatusClient.result(acs.document, acs.Variables(ConsignmentStatusInput(consignmentId, statusType, Some(statusValue)))).data
  }

  def updateConsignmentStatus(consignmentId: UUID, statusType: String, statusValue: String): Option[ucst.Data] = {
    val addConsignmentStatusClient = new UserApiClient[ucst.Data, ucst.Variables](userCredentials)
    addConsignmentStatusClient.result(ucst.document, ucst.Variables(ConsignmentStatusInput(consignmentId, statusType, Option(statusValue)))).data
  }

  def getConsignmentReference(consignmentId: UUID): String = {
    val client = new UserApiClient[gcs.Data, gcs.Variables](userCredentials)
    client.result(gcs.document, gcs.Variables(consignmentId)).data.get.getConsignment.get.consignmentReference
  }

  def createCustomMetadata(consignmentId: UUID): Unit = {
    val client = new UserApiClient[abfm.Data, abfm.Variables](userCredentials)
    val metadataProperties = getCustomMetadata(consignmentId)
      .filter(_.allowExport)
      .filter(cm => !List("ClientSideOriginalFilepath", "Filename").contains(cm.name))
    val files = getConsignmentExport(consignmentId).get.getConsignment.get.files.filter(!_.fileType.contains("Folder"))
    val fileIds =  files.map(_.fileId)
    val updateMetadata = metadataProperties.map(prop => {
      val value = prop.dataType match {
        case DataType.DateTime => "2022-09-28 14:31:17.746"
        case DataType.Text => s"${prop.name}-value"
        case DataType.Boolean => "true"
        case _ => "1"
      }
      UpdateFileMetadataInput(filePropertyIsMultiValue = true, prop.name, value)
    })
    val input = UpdateBulkFileMetadataInput(consignmentId, fileIds, updateMetadata)
    client.result(abfm.document, abfm.Variables(input))
  }

  def getCustomMetadata(consignmentId: UUID): List[cm.CustomMetadata] = {
    val client = new BackendApiClient[cm.Data, cm.Variables]()
    val variables = cm.Variables(consignmentId)
    client.sendRequest(cm.document, variables).data.get.customMetadata
  }

  def createAVMetadata(fileId: UUID, result: String = ""): Unit = {
    val client = new BackendApiClient[abavmd.Data, abavmd.Variables]
    val inputValues = List(
      AddAntivirusMetadataInputValues(fileId, "E2E tests software", "E2E tests software version", "E2E test DB version", result, System.currentTimeMillis)
    )
    val input = new AddAntivirusMetadataInput(inputValues)
      client.sendRequest(abavmd.document, abavmd.Variables(input))
  }


  def createBackendChecksumMetadata(consignmentId: UUID, fileIds: List[UUID], checksumValue: Option[String]): Unit = {
    val client = new BackendApiClient[abfm.Data, abfm.Variables]
    val input = UpdateBulkFileMetadataInput(consignmentId, fileIds, metadataProperties = List(UpdateFileMetadataInput(true, "SHA256ServerSideChecksum", checksumValue.getOrElse("checksumValue"))))
    client.sendRequest(abfm.document, abfm.Variables(input))
  }

  def createFfidMetadata(fileId: UUID, puid: String = "x-fmt/111"): Unit = {
    val client = new BackendApiClient[abffm.Data, abffm.Variables]
    val ffidInputMatches = FFIDMetadataInputMatches(Some("txt"), "e2e-test-basis", Some(puid), Some(false), Some(""))
    val input = FFIDMetadataInputValues(
      fileId,
      "e2e-test-software",
      "e2e-test-software-version",
      "e2e-test-binary-signature-file",
      "e2e-test-container-signature.xml",
      "e2e-test-method",
      List(ffidInputMatches))
    val variables = FFIDMetadataInput(List(input))
    client.sendRequest(abffm.document, abffm.Variables(variables))
  }


  def getConsignmentExport(consignmentId: UUID): Option[gcfe.Data] = {
    val client = new UserApiClient[gcfe.Data, gcfe.Variables](userCredentials)
    client.result(gcfe.document, gcfe.Variables(consignmentId)).data
  }

  def saveMetadata(consignmentId: UUID, fileIds: List[UUID], metadataType: String): Option[abfm.Data] = {
    val client = new UserApiClient[abfm.Data, abfm.Variables](userCredentials)
    val metadataInput = if (metadataType == "descriptive") {
      List(UpdateFileMetadataInput(filePropertyIsMultiValue = false, "description", "test description"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = false, "end_date", "2023-03-08 00:00:00.0"))
    } else {
      List(UpdateFileMetadataInput(filePropertyIsMultiValue = false, "FoiExemptionAsserted", "2000-01-01 00:00:00.0"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = false, "ClosureStartDate", "2000-01-01 00:00:00.0"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = false, "ClosurePeriod", "5"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = true, "FoiExemptionCode", "27(1)"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = false, "TitleClosed", "false"),
        UpdateFileMetadataInput(filePropertyIsMultiValue = false, "DescriptionClosed", "false"))
    }
    val updateBulkFileMetadataInput = UpdateBulkFileMetadataInput(consignmentId, fileIds, metadataInput)
    client.result(abfm.document, abfm.Variables(updateBulkFileMetadataInput)).data
  }

  def getDisplayProperties(consignmentId: UUID): Option[dp.Data] = {
    val client = new UserApiClient[dp.Data, dp.Variables](userCredentials)
    client.result(dp.document, dp.Variables(consignmentId)).data
  }
}

object GraphqlUtility {
  case class MatchIdInfo(checksum: String, path: Path, matchId: Int)
  def apply(userCredentials: UserCredentials): GraphqlUtility = new GraphqlUtility(userCredentials)
}
