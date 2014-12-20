import sbt._

object SWSPlugins extends Build {
  val plugins = Project("SWSPlugins", file("."))
    .settings(addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8"))
}