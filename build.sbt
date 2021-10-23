name := "refreshAKKA"

version := "0.1"

scalaVersion := "2.13.6"

val AkkaVersion = "2.6.16"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"         % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion,
  "org.scalatest"     %% "scalatest"                % "3.2.10",
  "ch.qos.logback"     % "logback-classic"          % "1.1.3"
)
