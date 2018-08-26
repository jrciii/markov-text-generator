name := "markov-text-generator"

version := "0.1"

scalaVersion := "2.12.6"

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"
// https://mvnrepository.com/artifact/org.iq80.leveldb/leveldb
libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.10"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
