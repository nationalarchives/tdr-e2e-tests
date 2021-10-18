import sbt._

object Dependencies {
  private val awsSdkVersion = "2.15.35"
  private val circeVersion = "0.13.0"
  private val cucumberVersion = "4.7.1"
  private val keycloakVersion = "15.0.2"
  private val softwareMillVersion = "2.1.1"

  lazy val scalaTestPlusPlay   = "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"
  lazy val keycloakCore        = "org.keycloak" % "keycloak-core" % keycloakVersion
  lazy val keycloakAdminClient = "org.keycloak" % "keycloak-admin-client" % keycloakVersion
  lazy val tdrGraphQlClient    = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.15"
  lazy val tdrGenerateGraphQl  = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.147"
  lazy val circeCore           = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric        = "io.circe" %% "circe-generic" % circeVersion
  lazy val softwareMillCore    = "com.softwaremill.sttp.client" %% "core" % softwareMillVersion
  lazy val softwareMillCirce   = "com.softwaremill.sttp.client" %% "circe" % softwareMillVersion
  lazy val awsSdkS3            = "software.amazon.awssdk" % "s3" % awsSdkVersion
  lazy val awsSdkSts           = "software.amazon.awssdk" % "sts" % awsSdkVersion
  lazy val cucumberCore        = "io.cucumber" % "cucumber-core" % cucumberVersion
  lazy val cucumberScala       = "io.cucumber" %% "cucumber-scala" % cucumberVersion
  lazy val cucumberJunit       = "io.cucumber" % "cucumber-junit" % cucumberVersion
}
