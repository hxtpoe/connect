import play.PlayScala
import sbt._

name := "connect"

scalaVersion := "2.11.1"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  cache,
  ws,
  "com.google.code.gson" % "gson" % "2.2.4",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
  "org.reactivecouchbase" %% "reactivecouchbase-play" % "0.3",
  "com.nimbusds" % "nimbus-jose-jwt" % "2.22.1",
  "com.wordnik" %% "swagger-play2" % "1.3.12",
  "com.rabbitmq" % "amqp-client" % "3.2.2"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "netty" at "http://mvnrepository.com/",
  "ReactiveCouchbase" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

lazy val root = project.in(file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= appDependencies
  )