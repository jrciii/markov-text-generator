name := "markov-text-generator"

version := "0.1"

scalaVersion := "2.12.6"

// https://mvnrepository.com/artifact/net.openhft/chronicle-map
libraryDependencies += "net.openhft" % "chronicle-map" % "3.16.0"
// https://mvnrepository.com/artifact/org.apache.commons/commons-compress
libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18"
// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"