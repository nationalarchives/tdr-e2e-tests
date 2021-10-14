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

  private def keyCloakAdminClient(): Keycloak = KeycloakBuilder.builder()
    .serverUrl(s"$authUrl/auth")
    .realm("tdr")
    .clientId(userAdminClient)
    .clientSecret(userAdminSecret)
    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
    .build()

  private def realmResource(client: Keycloak): RealmResource = client.realm("tdr")
  private def userResource(realm: RealmResource): UsersResource = realm.users()

  def createUser(userCredentials: UserCredentials, body: Option[String] = Some("Mock 1 Department")): String = {
    val client = keyCloakAdminClient()
    val realm = realmResource(client)
    val user = userResource(realm)

    val userRepresentation: UserRepresentation = new UserRepresentation

    val credentials: CredentialRepresentation = new CredentialRepresentation
    credentials.setTemporary(false)
    credentials.setType(CredentialRepresentation.PASSWORD)
    credentials.setValue(userCredentials.password)

    val creds = List(credentials).asJava

    userRepresentation.setUsername(userCredentials.userName)
    userRepresentation.setFirstName(userCredentials.firstName)
    userRepresentation.setLastName(userCredentials.lastName)
    userRepresentation.setEnabled(true)
    userRepresentation.setCredentials(creds)

    body match {
      case Some("Mock 4 Department") => userWithBodyAndNoSeries(userRepresentation)
      case Some(value) =>
        userRepresentation.setGroups(List(s"/transferring_body_user/$value").asJava)
      case _ => //do nothing
    }

    val response: Response = user.create(userRepresentation)

    val path = response.getLocation.getPath.replaceAll(".*/([^/]+)$", "$1")
    client.close()
    path
  }

  private def userWithBodyAndNoSeries(ur: UserRepresentation): Unit = {
    ur.setRealmRoles(List("tdr_user").asJava)
    ur.setAttributes(Map("body" -> List("MOCK4").asJava).asJava)
  }

  def deleteUser(userId: String): Unit = {
    val client = keyCloakAdminClient()
    val realm = realmResource(client)
    val user = userResource(realm)

    user.delete(userId)
    client.close()
  }
}

case class UserCredentials(userName: String,
                           password: String,
                           firstName: String = "Test First Name",
                           lastName: String = "Test Last Name")
