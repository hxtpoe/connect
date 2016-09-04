import play.PlayScala
import sbt._

name := "connect"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  cache,
  ws,
  "com.google.code.gson" % "gson" % "2.2.4",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
  "org.reactivecouchbase" %% "reactivecouchbase-play" % "0.3",
  "com.nimbusds" % "nimbus-jose-jwt" % "2.22.1",
  "com.wordnik" %% "swagger-play2" % "1.3.12",
  "com.rabbitmq" % "amqp-client" % "3.2.2",
  "joda-time" % "joda-time" % "2.9.4",
  "org.specs2" %% "specs2-core" % "2.4.17" % "test",
  "org.specs2" %% "specs2-mock" % "2.4.17" % "test",
  "org.specs2" %% "specs2-junit" % "2.4.17" % "test",
  "com.couchbase.client" % "java-client" % "2.1.3",
  "io.reactivex" % "rxscala_2.11" % "0.24.1"
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "netty" at "http://mvnrepository.com/",
  "ReactiveCouchbase" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "couchbase repo" at "http://files.couchbase.com/maven2"
)

lazy val root = project.in(file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= appDependencies
  )

val initusers = TaskKey[Unit]("initusers", "init users")

initusers := {
  println("ToDo")
}
