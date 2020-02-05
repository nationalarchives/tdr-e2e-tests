package helpers.users

import com.typesafe.config.ConfigFactory
import javax.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.{RealmResource, UsersResource}
import org.keycloak.representations.idm.{CredentialRepresentation, UserRepresentation}

import scala.jdk.CollectionConverters._

object KeycloakClient {

  private val configuration = ConfigFactory.load()
  private val authUrl: String = configuration.getString("tdr.auth.url")
  private val keyCloakAdminUser: String = System.getProperty("keycloak.user")
  private val keyCloakAdminPassword: String = System.getProperty("keycloak.password")

  private val keyCloakAdminClient: Keycloak = Keycloak.getInstance(
    s"$authUrl/auth",
    "master",
    s"$keyCloakAdminUser",
    s"$keyCloakAdminPassword",
    "admin-cli"
  )

  private val realm: RealmResource = keyCloakAdminClient.realm("tdr")
  private val userResource: UsersResource = realm.users()

  def createUser(userName: String, password: String): String = {

    val userRepresentation: UserRepresentation = new UserRepresentation

    val credentials: CredentialRepresentation = new CredentialRepresentation
    credentials.setTemporary(false)
    credentials.setType(CredentialRepresentation.PASSWORD)
    credentials.setValue(password)

    val creds = List(credentials).asJava

    userRepresentation.setUsername(userName)
    userRepresentation.setEnabled(true)
    userRepresentation.setCredentials(creds)

    val response: Response = userResource.create(userRepresentation)

    response.getLocation.getPath.replaceAll(".*/([^/]+)$", "$1")
  }

  def deleteUser(userId: String): Unit = {
    userResource.get(userId).remove
  }
}
