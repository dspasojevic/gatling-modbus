import sbt._

object Dependencies {
  lazy val GatlingVersion = "2.3.0"

  lazy val GatlingDependencies = Seq(
    "io.gatling" % "gatling-core" % GatlingVersion
  )
}
