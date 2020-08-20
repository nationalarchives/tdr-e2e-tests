package helpers.graphql

import java.util.UUID

import graphql.codegen.AddConsignment.{addConsignment => ac}
import graphql.codegen.GetSeries.{getSeries => gs}
import graphql.codegen.AddTransferAgreement.{AddTransferAgreement => ata}
import graphql.codegen.types.{AddConsignmentInput, AddTransferAgreementInput}
import helpers.users.UserCredentials

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


}

object GraphqlUtility {
  def apply(userCredentials: UserCredentials): GraphqlUtility = new GraphqlUtility(userCredentials)
}
