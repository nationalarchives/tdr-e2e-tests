name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "org.keycloak" % "keycloak-core" % "7.0.1" % Test
libraryDependencies += "org.keycloak" % "keycloak-admin-client" % "7.0.1" % Test

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" % Test
libraryDependencies += "org.jboss.resteasy" % "resteasy-client" % "3.6.0.Final" % Test
libraryDependencies += "org.jboss.resteasy" % "resteasy-jackson2-provider" % "3.6.0.Final" % Test
libraryDependencies += "org.jboss.resteasy" % "resteasy-multipart-provider" % "3.6.0.Final" % Test


libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % "4.7.1" % "test",
  "io.cucumber" %% "cucumber-scala" % "4.7.1" % "test",
  "io.cucumber" % "cucumber-junit" % "4.7.1" % "test"
)