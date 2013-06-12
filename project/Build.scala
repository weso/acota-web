import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "acota-web"
  val appVersion = "0.3.8-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.apache.mahout" % "mahout-integration" % "0.7" exclude ("org.mongodb", "bson"),
    "org.mongodb" % "bson" % "2.11.1",
    "es.weso" % "acota-core" % "0.3.8-SNAPSHOT",
    "es.weso" % "acota-feedback" % "0.3.8-SNAPSHOT",
    "es.weso" % "acota-utils" % "0.3.8-SNAPSHOT")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += "Open NLP Repository" at "http://opennlp.sourceforge.net/maven2")

}
