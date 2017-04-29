lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "electricity_administration",
    version := "1.1.0",
    scalaVersion := "2.11.8"
  )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.0",
  "com.github.t3hnar" %% "scala-bcrypt" % "3.0",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "com.pauldijou" %% "jwt-play-json" % "0.12.1"
)
