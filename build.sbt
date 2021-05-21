name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "org.keycloak" % "keycloak-core" % "11.0.3" % Test
libraryDependencies += "org.keycloak" % "keycloak-admin-client" % "11.0.3" % Test
libraryDependencies += "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.15"
libraryDependencies += "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.88"
libraryDependencies += "io.circe" %% "circe-core" % "0.13.0"
libraryDependencies += "io.circe" %% "circe-generic" % "0.13.0"
libraryDependencies += "com.softwaremill.sttp.client" %% "core" % "2.1.1"
libraryDependencies += "com.softwaremill.sttp.client" %% "circe" % "2.1.1"
libraryDependencies += "software.amazon.awssdk" % "s3" % "2.15.35"
libraryDependencies += "software.amazon.awssdk" % "sts" % "2.15.35"

libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % "4.7.1" % "test",
  "io.cucumber" %% "cucumber-scala" % "4.7.1" % "test",
  "io.cucumber" % "cucumber-junit" % "4.7.1" % "test"
)

resolvers += "TDR Releases" at "s3://tdr-releases-mgmt"
