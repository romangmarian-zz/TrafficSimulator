lazy val akkaHttpV = "10.1.7"
lazy val akkaV = "2.5.21"
lazy val cassandraDriverV = "3.3.0"
lazy val swaggerAkkaV = "0.11.2"
lazy val geocalcV = "1.1.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "nadstrif",
      scalaVersion := "2.12.7"
    )),
    name := "scala-microservice",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
      "com.typesafe.akka" %% "akka-stream" % akkaV,
      "com.typesafe.akka" %% "akka-actor" % akkaV,
      "com.typesafe.akka" %% "akka-slf4j" % akkaV,
      "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,

      "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverV exclude("org.xerial.snappy", "snappy-java"),

      "com.github.swagger-akka-http" %% "swagger-akka-http" % swaggerAkkaV,

      "com.peertopark.java" % "geocalc" % geocalcV,

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,

      "com.softwaremill.common" %% "id-generator" % "1.2.1"
    )
  )
