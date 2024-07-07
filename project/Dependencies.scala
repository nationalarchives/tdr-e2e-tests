import sbt._

object Dependencies {
  private val awsSdkVersion = "2.26.11"
  private val circeVersion = "0.14.8"
  private val cucumberCoreVersion = "7.18.0"
  private val cucumberScalaVersion = "8.23.0"
  private val cucumberJUnitVersion = "7.18.0"
  private val keycloakVersion = "25.0.1"
  private val softwareMillVersion = "2.3.0"


  lazy val tdrGraphQlClient    = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.169"
  lazy val scalaTestPlusPlay   = "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1"
  lazy val keycloakCore        = "org.keycloak" % "keycloak-core" % keycloakVersion
  lazy val keycloakAdminClient = "org.keycloak" % "keycloak-admin-client" % keycloakVersion
  lazy val tdrGenerateGraphQl  = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.377"
  lazy val circeCore           = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric        = "io.circe" %% "circe-generic" % circeVersion
  lazy val softwareMillCore    = "com.softwaremill.sttp.client" %% "core" % softwareMillVersion
  lazy val softwareMillCirce   = "com.softwaremill.sttp.client" %% "circe" % softwareMillVersion
  lazy val awsSdkS3            = "software.amazon.awssdk" % "s3" % awsSdkVersion
  lazy val awsSdkSts           = "software.amazon.awssdk" % "sts" % awsSdkVersion
  lazy val scalaCsv            = "com.github.tototoshi" %% "scala-csv" % "1.4.0"
  lazy val cucumberCore        = "io.cucumber" % "cucumber-core" % cucumberCoreVersion % "test"
  lazy val cucumberScala       = "io.cucumber" %% "cucumber-scala" % cucumberScalaVersion % "test"
  lazy val cucumberJunit       = "io.cucumber" % "cucumber-junit" % cucumberJUnitVersion % "test"
}
