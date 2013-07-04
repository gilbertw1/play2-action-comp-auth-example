import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play2-action-comp-auth-example"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
