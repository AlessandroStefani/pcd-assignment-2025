ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"
resolvers += "Akka library repository".at("https://repo.akka.io/maven")
lazy val akkaVersion = "2.8.8"
lazy val root = (project in file("."))
  .settings(
    name := "agar-io",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.8.8",

    libraryDependencies +="com.typesafe.akka" %% "akka-actor-typed" % "2.8.8", // For standard log configuration
    libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.8.8", // For akka remote
    libraryDependencies +="com.typesafe.akka" %% "akka-cluster-typed" % "2.8.8", // akka clustering module
    libraryDependencies +="com.typesafe.akka" %% "akka-serialization-jackson" % "2.8.8",
    libraryDependencies +="com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.8" % Test,
    libraryDependencies +="ch.qos.logback" % "logback-classic" % "1.5.19",
    libraryDependencies +="org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies +="com.typesafe.akka" %% "akka-actor-typed" % "2.8.8"

  )
