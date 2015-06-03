name := "jvmscripter"

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-deprecation") // add implicits

//retrieveManaged := true

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-caching" % "1.3.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9",
  //"rhino" % "js" % "1.7R2" % "runtime",
  "org.scala-lang" % "scala-compiler" % "2.11.6",
  "org.codehaus.groovy" % "groovy-all" % "2.4.3" % "runtime",
  //"org.beanshell" % "bsh" % "2.0b5" % "runtime",
  //"org.jruby" % "jruby-complete" % "9.0.0.0.pre2" % "runtime",
  //"org.python" % "jython-standalone" % "2.7.0" % "runtime",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7" ,//% "provided",
  "org.scalatest" %% "scalatest" % "3.0.0-SNAP4" % "test"
)

initialCommands in console := """
    |import xrrocha.util._
    |import xrrocha.jvmscripter._
    |""".stripMargin
