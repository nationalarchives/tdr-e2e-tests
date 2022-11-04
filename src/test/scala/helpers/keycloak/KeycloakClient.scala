package helpers.keycloak

import cats.implicits._
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import sttp.client3.circe._
import  sttp.client3.{HttpURLConnectionBackend, Identity, Response, SttpBackend, UriContext, basicRequest}

import scala.language.postfixOps

object KeycloakClient {
  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  private val configuration = ConfigFactory.load()
  private val userAdminClient: String = configuration.getString("keycloak.user.admin.client")
  private val userAdminSecret: String = configuration.getString("keycloak.user.admin.secret")
  private val userApiUrl: String = configuration.getString("keycloak.user.api.url")

  def getToken: String = {

    val authUrl = configuration.getString("tdr.auth.url")

    val response = basicRequest
      .post(uri"$authUrl/realms/tdr/protocol/openid-connect/token")
      .auth.basic(userAdminClient, userAdminSecret)
      .body(Map("grant_type" -> "client_credentials"))
      .response(asJson[AuthResponse])
      .send(backend)
    println(">>>>>Auth - " + authUrl)
    println(">>>>>userAdminClient - " + userAdminClient)
    println(">>>>>userAdminSecret - " + userAdminSecret)

    response.body match {
      case Right(body) =>
        body.access_token
      case Left(e) => throw e
    }
  }

  def createUser(
                  userCredentials: UserCredentials,
                  body: Option[String],
                  userType: Option[String] = None): String = {

    case class UserApiRequest(email: String, password: Option[String] = None,
                              firstName: String, lastName: String, body: Option[String] = None,
                              userType: Option[String] = None)

    val requestBody = UserApiRequest(userCredentials.email, userCredentials.password.some, userCredentials.firstName, userCredentials.lastName, body, userType)

    val response: Identity[Response[Either[String, String]]] = basicRequest
      .body(requestBody)
      .auth.bearer(getToken)
      .post(uri"$userApiUrl")
      .send()

    response.body match {
      case Left(err) => throw new Exception(err)
      case Right(id) => id
    }
  }

  def deleteUser(userId: String): String = {
    val response: Identity[Response[Either[String, String]]] = basicRequest
      .auth.bearer(getToken)
      .delete(uri"$userApiUrl/$userId")
      .send()
    response.body match {
      case Left(err) => throw new Exception(err)
      case Right(id) => id
    }
  }
}

case class UserCredentials(email: String,
                           password: String,
                           firstName: String = "Test First Name",
                           lastName: String = "Test Last Name")
