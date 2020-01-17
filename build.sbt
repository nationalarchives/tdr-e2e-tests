name := "tdr-e2e-tests"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies ++= Seq(
  "io.cucumber" % "cucumber-core" % "4.7.1" % "test",
  "io.cucumber" %% "cucumber-scala" % "4.7.1" % "test",
  "io.cucumber" % "cucumber-junit" % "4.7.1" % "test"
)