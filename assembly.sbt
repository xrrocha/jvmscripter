import AssemblyKeys._

seq(assemblySettings: _*)

//excludedJars in assembly := {
//  val cp = (fullClasspath in assembly).value
//  cp filter {_.data.getName == "spark-core_2.9.3-0.8.0-incubating.jar"}
//}
