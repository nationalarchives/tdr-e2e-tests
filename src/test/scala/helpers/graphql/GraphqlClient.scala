package helpers.graphql

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.ConfigFactory
import helpers.keycloak.{KeycloakUtility, UserCredentials}
import io.circe.{Decoder, Encoder}
import sangria.ast.Document
import uk.gov.nationalarchives.tdr.{GraphQLClient, GraphQlResponse}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class GraphqlClient[Data, Variables](userCredentials: UserCredentials)(implicit val decoder: Decoder[Data], val encoder: Encoder[Variables]) {
  val configuration = ConfigFactory.load
  val body: Map[String, String] = Map(
    "grant_type" -> "password",
    "username" -> userCredentials.userName,
    "password" -> userCredentials.password,
    "client_id" -> "tdr-fe"
  )

  def userToken: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(body)
  }

  def result(document: Document, variables: Variables) = {
    val client = new GraphQLClient[Data, Variables](configuration.getString("tdr.api.url"))
    Await.result(client.getResult(userToken, document, Some(variables)), 10 seconds)
  }
}

object GraphqlClient {
  def apply[Data, Variables](userCredentials: UserCredentials)(implicit decoder: Decoder[Data], encoder: Encoder[Variables]): GraphqlClient[Data, Variables] = new GraphqlClient(userCredentials)(decoder, encoder)
}
