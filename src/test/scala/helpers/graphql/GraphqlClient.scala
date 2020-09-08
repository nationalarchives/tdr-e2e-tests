package helpers.graphql

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.ConfigFactory
import helpers.keycloak.KeycloakClient.configuration
import helpers.keycloak.{KeycloakUtility, UserCredentials}
import io.circe.{Decoder, Encoder}
import sangria.ast.Document
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}
import uk.gov.nationalarchives.tdr.{GraphQLClient, GraphQlResponse}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class GraphqlClient[Data, Variables](userCredentials: UserCredentials)(implicit val decoder: Decoder[Data], val encoder: Encoder[Variables]) {
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  val configuration = ConfigFactory.load

  val body: Map[String, String] = Map(
    "grant_type" -> "password",
    "username" -> userCredentials.userName,
    "password" -> userCredentials.password,
    "client_id" -> "tdr-fe"
  )

  private def userToken: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(body)
  }

  def result(document: Document, variables: Variables): GraphQlResponse[Data] = {
    GraphqlClient.sendApiRequest(document, variables, userToken)
  }
}

class BackendApiClient[Data, Variables](implicit val decoder: Decoder[Data], val encoder: Encoder[Variables]) {
  val configuration = ConfigFactory.load
  private val backendChecksSecret: String = configuration.getString("keycloak.backendchecks.secret")

  private def backendChecksToken: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(Map(
      "grant_type" -> "client_credentials",
      "client_id" -> "tdr-backend-checks",
      "client_secret" -> s"${backendChecksSecret}"
    ))
  }

  def sendRequest(document: Document, variables: Variables): GraphQlResponse[Data] = {
    GraphqlClient.sendApiRequest(document, variables, backendChecksToken)
  }
}

object GraphqlClient {
  def apply[Data, Variables](userCredentials: UserCredentials)(implicit decoder: Decoder[Data], encoder: Encoder[Variables]): GraphqlClient[Data, Variables] = new GraphqlClient(userCredentials)(decoder, encoder)

  implicit private val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  private val configuration = ConfigFactory.load

  def sendApiRequest[Data, Variables](document: Document, variables: Variables, token: BearerAccessToken)(implicit decoder: Decoder[Data], encoder: Encoder[Variables]) = {
    val client = new GraphQLClient[Data, Variables](configuration.getString("tdr.api.url"))
    Await.result(client.getResult(token, document, Some(variables)), 10 seconds)
  }
}
