package helpers.keycloak

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.ConfigFactory
import javax.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.{RealmResource, UsersResource}
import org.keycloak.representations.idm.{CredentialRepresentation, UserRepresentation}
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object KeycloakClient {
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private val configuration = ConfigFactory.load()
  private val authUrl: String = configuration.getString("tdr.auth.url")
  private val realmClientSecret: String = System.getProperty("keycloak.realm.secret")
  private val token: BearerAccessToken = {
    KeycloakUtility.bearerAccessToken(Map(
      "grant_type" -> "client_credentials",
      "client_id" -> "tdr-realm-admin",
      "client_secret" -> realmClientSecret
    ))
  }

  private val keyCloakAdminClient: Keycloak = Keycloak.getInstance(
    s"$authUrl/auth",
    "tdr",
    "tdr-realm-admin",
    token.getValue
  )

  private val realm: RealmResource = keyCloakAdminClient.realm("tdr")
  private val userResource: UsersResource = realm.users()

  def createUser(userCredentials: UserCredentials, body: Option[String] = Some("MOCK1")): String = {

    val userRepresentation: UserRepresentation = new UserRepresentation

    val credentials: CredentialRepresentation = new CredentialRepresentation
    credentials.setTemporary(false)
    credentials.setType(CredentialRepresentation.PASSWORD)
    credentials.setValue(userCredentials.password)

    val creds = List(credentials).asJava

    userRepresentation.setUsername(userCredentials.userName)
    userRepresentation.setEnabled(true)
    userRepresentation.setCredentials(creds)
    body.foreach(b => userRepresentation.setAttributes(Map("body" -> List(b).asJava).asJava))
    userRepresentation.setRealmRoles(List("tdr_user").asJava)

    val response: Response = userResource.create(userRepresentation)

    response.getLocation.getPath.replaceAll(".*/([^/]+)$", "$1")
  }

  def deleteUser(userId: String): Unit = {
    userResource.get(userId).remove
  }
}

case class UserCredentials(userName: String, password: String)
