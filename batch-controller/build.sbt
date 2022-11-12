name := """batch-controller"""
organization := "io.camunda.batching.broker"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

useCoursier := false

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "io.camunda.batching.messaging" % "messaging-package" % "1.0" exclude ("com.fasterxml.jackson.core", "jackson-databind")
libraryDependencies += "io.camunda" % "zeebe-client-java" % "8.1.0-sytse-local" exclude ("com.fasterxml.jackson.core", "jackson-databind")

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.5.0"

libraryDependencies ++= Seq(
javaJdbc
)

// required for finding local maven packages
resolvers += Resolver.mavenLocal

// docker config
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

Docker / maintainer := "s.oegema@student.rug.nl"
Docker / packageName := "batch-controller"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "0")
Docker / daemonUserUid  := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
dockerBaseImage := "openjdk:11-jre-slim"
dockerRepository := sys.env.get("ecr_repo")
dockerUpdateLatest := true
