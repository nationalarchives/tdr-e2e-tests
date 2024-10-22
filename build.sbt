import Dependencies._

name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  scalaTestPlusPlay % Test,
  keycloakCore % Test,
  keycloakAdminClient % Test,
  tdrGraphQlClient,
  tdrGenerateGraphQl,
  circeCore,
  circeGeneric,
  fastexcelReader,
  softwareMillCore,
  softwareMillCirce,
  awsSdkS3,
  awsSdkSts,
  scalaCsv,
  cucumberCore,
  cucumberScala,
  cucumberJunit
)

resolvers += "TDR Releases" at "s3://tdr-releases-mgmt"
