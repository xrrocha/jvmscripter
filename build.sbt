name := "jvmscripter"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers ++=  Seq(
    "Spray Repo" at "http://repo.spray.io",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

scalacOptions ++= Seq("-deprecation", "-language:experimental.macros")

retrieveManaged := true

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.2-RC4",
  "io.spray" % "spray-routing" % "1.2-RC4",
  "io.spray" % "spray-caching" % "1.2-RC4",
  "io.spray" %% "spray-json" % "1.2.5",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  //"rhino" % "js" % "1.7R2" % "runtime",
  "org.codehaus.groovy" % "groovy-all" % "2.2.0-rc-3" % "runtime",
  //"org.beanshell" % "bsh" % "2.0b5" % "runtime",
  //"org.jruby" % "jruby" % "1.7.8" % "runtime",
  //"org.python" % "jython-standalone" % "2.7-b1" % "runtime",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5" ,//% "provided",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)
