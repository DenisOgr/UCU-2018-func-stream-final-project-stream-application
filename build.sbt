name := "UCU-2018-func-stream-final-project-stream-application"

version := "0.1"

scalaVersion := "2.12.8"
val kafkaGroupId = "org.apache.kafka"
val kafkaClientArtifactId = "kafka-clients"
val kafkaClientRevision = "0.10.1.0"
libraryDependencies +=  kafkaGroupId % kafkaClientArtifactId % kafkaClientRevision

val kafkaConnectArtifactId = "connect-api"
libraryDependencies +=  kafkaGroupId % kafkaConnectArtifactId % kafkaClientRevision

resolvers += Resolver.bintrayRepo("ovotech", "maven")
val kafkaSerializationV = "0.3.9"

libraryDependencies ++= Seq(
  "com.ovoenergy" %% "kafka-serialization-core" % kafkaSerializationV,
  "com.ovoenergy" %% "kafka-serialization-circe" % kafkaSerializationV
)

val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

fork in run := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
