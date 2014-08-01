name := "jvmscripter"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"

resolvers ++=  Seq(
    "Spray Repo" at "http://repo.spray.io",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

scalacOptions ++= Seq("-deprecation")

retrieveManaged := true

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % "1.3.1",
  "io.spray" %% "spray-routing" % "1.3.1",
  "io.spray" %% "spray-caching" % "1.3.1",
  "io.spray" %% "spray-json" % "1.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.2",
  //"rhino" % "js" % "1.7R2" % "runtime",
  "org.codehaus.groovy" % "groovy-all" % "2.3.6" % "runtime",
  //"org.beanshell" % "bsh" % "2.0b5" % "runtime",
  //"org.jruby" % "jruby" % "1.7.13" % "runtime",
  //"org.python" % "jython-standalone" % "2.7-b2" % "runtime",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7" ,//% "provided",
  "org.scalatest" %% "scalatest" % "2.2.1-M3" % "test"
)

initialCommands in console := """
    |import xrrocha.util._
    |import xrrocha.jvmscripter._
    |""".stripMargin
