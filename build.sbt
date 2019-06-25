name := "restaurant"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.6.0-M1"
resolvers in ThisBuild += Resolver.bintrayRepo("streetcontxt", "maven")

libraryDependencies ++= Seq(
                             "com.typesafe.akka" %% "akka-actor" % akkaVersion,
                             "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
                             "org.scalatest" %% "scalatest" % "3.0.5" % "test",
                             "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
                             "com.typesafe.akka" %% "akka-stream" % akkaVersion,
                             "com.streetcontxt" %% "kcl-akka-stream" % "2.0.3",
                             "com.lightbend.akka" %% "akka-stream-alpakka-kinesis" % "1.0.2",
                             "com.streetcontxt" %% "kpl-scala" % "1.0.5",
                             "org.slf4j" % "slf4j-simple" % "1.6.4",
                             "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.9",
                             "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.9",
                             "com.github.etaty" %% "rediscala" % "1.8.0"


                           )