name := "sbt-simple-avrohugger"
organization in ThisBuild := "com.mariussoutier.sbt"

description := "Simplified Avro generator based on Avrohugger"
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions += "-deprecation"

sbtPlugin := true
crossSbtVersions := Seq("0.13.16", "1.1.0")

publishMavenStyle := false
bintrayOrganization in bintray := None
bintrayRepository := "sbt-plugins"

libraryDependencies ++= Seq(
  "com.julianpeeters" %% "avrohugger-core" % "0.18.0",
  "com.julianpeeters" %% "avrohugger-filesorter" % "0.18.0"
)
