package helpers.keycloak

import com.typesafe.config.ConfigFactory
import javax.ws.rs.core.Response
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.resource.{RealmResource, UsersResource}
import org.keycloak.admin.client.{Keycloak, KeycloakBuilder}
import org.keycloak.representations.idm.{CredentialRepresentation, UserRepresentation}
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object KeycloakClient {
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private val configuration = ConfigFactory.load()
  private val authUrl: String = configuration.getString("tdr.auth.url")
  private val userAdminClient: String = configuration.getString("keycloak.user.admin.client")
  private val userAdminSecret: String = configuration.getString("keycloak.user.admin.secret")

  private val keyCloakAdminClient: Keycloak = KeycloakBuilder.builder()
    .serverUrl(s"$authUrl/auth")
    .realm("tdr")
    .clientId(userAdminClient)
    .clientSecret(userAdminSecret)
    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
    .build()

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
    userResource.delete(userId)
  }
}

case class UserCredentials(userName: String, password: String)
