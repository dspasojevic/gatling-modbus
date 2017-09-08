import Dependencies._

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "au.com.agiledigital",
      scalaVersion := "2.12.3",
      version := "0.1.0-SNAPSHOT"
    )),
  name := "gatling-modbus",
  libraryDependencies ++= Seq(
    "com.digitalpetri.modbus" % "modbus-master-tcp"   % "1.1.0",
    "org.scala-lang.modules"  %% "scala-java8-compat" % "0.8.0"
  ) ++ GatlingDependencies
)
