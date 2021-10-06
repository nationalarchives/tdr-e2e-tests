name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.1"

lazy val cucumberVersion = "6.11.0"

libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-firefox-driver" % "4.0.0-rc-2"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-chrome-driver" % "4.0.0-rc-2"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-support" % "4.0.0-rc-2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % Test
libraryDependencies += "org.keycloak" % "keycloak-core" % "11.0.3" % Test
libraryDependencies += "org.keycloak" % "keycloak-admin-client" % "11.0.3" % Test
libraryDependencies += "uk.gov.nationalarchives" %% "tdr-graphql-client" % "0.0.15"
libraryDependencies += "uk.gov.nationalarchives" %% "tdr-generated-graphql" % "0.0.147"
libraryDependencies += "io.circe" %% "circe-core" % "0.13.0"
libraryDependencies += "io.circe" %% "circe-generic" % "0.13.0"
libraryDependencies += "com.softwaremill.sttp.client" %% "core" % "2.1.1"
libraryDependencies += "com.softwaremill.sttp.client" %% "circe" % "2.1.1"
libraryDependencies += "software.amazon.awssdk" % "s3" % "2.15.35"
libraryDependencies += "software.amazon.awssdk" % "sts" % "2.15.35"

libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % cucumberVersion % Test,
  "io.cucumber" %% "cucumber-scala" % "7.1.0" % Test,
  "io.cucumber" % "cucumber-junit" % cucumberVersion % Test
)

resolvers += "TDR Releases" at "s3://tdr-releases-mgmt"
