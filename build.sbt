import Dependencies._

name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.8"


assembly / assemblyOption ~= {
  _.withIncludeScala(false)
    .withIncludeDependency(false)
}
assembly / mainClass := Option("runners.Lambda")
enablePlugins(PackPlugin)

packMain := Map("lambda" -> "runners.Lambda")

libraryDependencies ++= Seq(
  keycloakCore,
  keycloakAdminClient,
  tdrGraphQlClient,
  tdrGenerateGraphQl,
  "org.scalatest" %% "scalatest" % "3.2.12",
  "org.seleniumhq.selenium" % "selenium-java" % "4.2.0",
  "com.typesafe" % "config" % "1.4.2",
  "com.amazonaws" % "aws-lambda-java-runtime-interface-client" % "2.1.1",
  "com.github.sbt" % "junit-interface" % "0.13.2",
  circeCore,
  circeGeneric,
  softwareMillCore,
  softwareMillCirce,
  awsSdkS3,
  awsSdkSts,
  cucumberCore,
  cucumberScala,
  cucumberJunit,
  lambdaJavaCore,
  lambdaJavaEvents
)
assembly / fullClasspath := (assembly / fullClasspath).value ++ (Test / fullClasspath).value

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

(assemblyPackageDependency / assemblyMergeStrategy) := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}
