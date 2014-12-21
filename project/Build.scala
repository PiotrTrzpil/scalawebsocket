import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    homepage := Some(url("https://github.com/pbuda/scalawebsocket")),
    licenses := Seq("Apache License 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    organization := "eu.piotrbuda",
    name := "scalawebsocket",
    version := "0.1.1",
    scalaVersion := "2.11.4",
    publishMavenStyle := true,
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := {
      _ => false
    },
    pomExtra := (
      <scm>
        <url>git@github.com:pbuda/scalawebsocket.git</url>
        <connection>scm:git:git@github.com:pbuda/scalawebsocket.git</connection>
      </scm>
        <developers>
          <developer>
            <id>pbuda</id>
            <name>Piotr Buda</name>
            <url>http://www.piotrbuda.eu</url>
          </developer>
        </developers>
      )
  )
}

object Dependencies {
  val asynchttpclient = "com.ning" % "async-http-client" % "1.9.3"

  //logging
  val scalalogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"

  val logging = Seq(scalalogging, logback)

  //jetty is used to setup test server
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "8.1.16.v20140903"
  val jettyWebsocket = "org.eclipse.jetty" % "jetty-websocket" % "8.1.16.v20140903"
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "8.1.16.v20140903"
  val jettyServlets = "org.eclipse.jetty" % "jetty-servlets" % "8.1.16.v20140903"

  val jetty = Seq(jettyServer, jettyWebsocket, jettyServlet, jettyServlets)

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.1" % "test"
}

object SWSBuild extends Build {

  import Dependencies._
  import BuildSettings._

  val root = Project("scalawebsocket", file("."), settings = buildSettings)
    .settings(libraryDependencies := Seq(asynchttpclient, scalatest) ++ logging ++ jetty)
}