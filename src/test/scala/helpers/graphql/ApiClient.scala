package helpers.graphql

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.{Config, ConfigFactory}
import helpers.keycloak.{KeycloakUtility, UserCredentials}
import io.circe.{Decoder, Encoder}
import sangria.ast.Document
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}
import uk.gov.nationalarchives.tdr.{GraphQLClient, GraphQlResponse}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class UserApiClient[Data, Variables](userCredentials: UserCredentials)(implicit val decoder: Decoder[Data], val encoder: Encoder[Variables]) {
  val body: Map[String, String] = Map(
    "grant_type" -> "password",
    "username" -> userCredentials.email,
    "password" -> userCredentials.password,
    "client_id" -> "tdr-fe"
  )

  private def userToken: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(body)
  }

  def result(document: Document, variables: Variables): GraphQlResponse[Data] = {
    ApiClient.sendApiRequest(document, variables, userToken)
  }
}

class BackendApiClient[Data, Variables](implicit val decoder: Decoder[Data], val encoder: Encoder[Variables]) {
  val configuration: Config = ConfigFactory.load
  private val backendChecksSecret: String = configuration.getString("keycloak.backendchecks.secret")

  private def backendChecksToken: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(Map(
      "grant_type" -> "client_credentials",
      "client_id" -> "tdr-backend-checks",
      "client_secret" -> s"$backendChecksSecret"
    ))
  }

  def sendRequest(document: Document, variables: Variables): GraphQlResponse[Data] = {
    ApiClient.sendApiRequest(document, variables, backendChecksToken)
  }
}

object ApiClient {
  implicit private val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()
  private val configuration: Config = ConfigFactory.load

  def sendApiRequest[Data, Variables](document: Document, variables: Variables, token: BearerAccessToken)(implicit decoder: Decoder[Data], encoder: Encoder[Variables]): GraphQlResponse[Data] = {
    val client = new GraphQLClient[Data, Variables](configuration.getString("tdr.api.url"))
    Await.result(client.getResult(token, document, Some(variables)), 10 seconds)
  }
}
