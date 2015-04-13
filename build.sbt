name := "Teksto klasifikatorius"

organization := "org.vdu.snavickas"

version := "1.0"

scalaVersion := "2.10.5"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Apache" at "http://repo.maven.apache.org/maven2/"
)

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.11"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.2.1" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.2.1" % "provided"

libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.5.3"
