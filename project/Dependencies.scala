import sbt._

object Dependencies {
  private val awsSdkVersion = "2.17.162"
  private val circeVersion = "0.14.2"
  private val cucumberCoreVersion = "7.3.4"
  private val cucumberScalaVersion = "8.4.0"
  private val cucumberJUnitVersion = "7.3.4"
  private val keycloakVersion = "18.0.0"
  private val softwareMillVersion = "2.3.0"

  lazy val tdrGraphQlClient    = "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.35"
  lazy val scalaTestPlusPlay   = "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"
  lazy val keycloakCore        = "org.keycloak" % "keycloak-core" % keycloakVersion
  lazy val keycloakAdminClient = "org.keycloak" % "keycloak-admin-client" % keycloakVersion
  lazy val lambdaJavaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
  lazy val lambdaJavaEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.11.0"
  lazy val tdrGenerateGraphQl  = "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.243"
  lazy val circeCore           = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric        = "io.circe" %% "circe-generic" % circeVersion
  lazy val softwareMillCore    = "com.softwaremill.sttp.client" %% "core" % softwareMillVersion
  lazy val softwareMillCirce   = "com.softwaremill.sttp.client" %% "circe" % softwareMillVersion
  lazy val awsSdkS3            = "software.amazon.awssdk" % "s3" % awsSdkVersion
  lazy val awsSdkSts           = "software.amazon.awssdk" % "sts" % awsSdkVersion
  lazy val cucumberCore        = "io.cucumber" % "cucumber-core" % cucumberCoreVersion
  lazy val cucumberScala       = "io.cucumber" %% "cucumber-scala" % cucumberScalaVersion
  lazy val cucumberJunit       = "io.cucumber" % "cucumber-junit" % cucumberJUnitVersion
}
