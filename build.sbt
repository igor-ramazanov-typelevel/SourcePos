import org.typelevel.sbt.gha.WorkflowStep.Run
import org.typelevel.sbt.gha.WorkflowStep.Sbt

ThisBuild / githubOwner := "igor-ramazanov-typelevel"
ThisBuild / githubRepository := "SourcePos"

ThisBuild / githubWorkflowPublishPreamble := List.empty
ThisBuild / githubWorkflowUseSbtThinClient := true
ThisBuild / githubWorkflowPublish := List(
  Run(
    commands = List("echo \"$PGP_SECRET\" | gpg --import"),
    id = None,
    name = Some("Import PGP key"),
    env = Map("PGP_SECRET" -> "${{ secrets.PGP_SECRET }}"),
    params = Map(),
    timeoutMinutes = None,
    workingDirectory = None
  ),
  Sbt(
    commands = List("+ publish"),
    id = None,
    name = Some("Publish"),
    cond = None,
    env = Map("GITHUB_TOKEN" -> "${{ secrets.GB_TOKEN }}"),
    params = Map.empty,
    timeoutMinutes = None,
    preamble = true
  )
)
ThisBuild / gpgWarnOnFailure := false


ThisBuild / tlBaseVersion := "1.1"

// Our Scala versions.
lazy val `scala-2.13`     = "2.13.16"
lazy val `scala-3.0`      = "3.3.6"

ThisBuild / tlCiHeaderCheck := false

// Publishing
ThisBuild / organization := "org.tpolecat"
ThisBuild / licenses     := Seq(("MIT", url("http://opensource.org/licenses/MIT")))
ThisBuild / homepage     := Some(url("https://github.com/tpolecat/sourcepos"))
ThisBuild / developers   := List(
  Developer("tpolecat", "Rob Norris", "rob_norris@mac.com", url("http://www.tpolecat.org"))
)
ThisBuild / tlSonatypeUseLegacyHost := false

// Compilation
ThisBuild / scalaVersion       := `scala-2.13`
ThisBuild / crossScalaVersions := Seq(`scala-2.13`, `scala-3.0`)

lazy val root = tlCrossRootProject.aggregate(sourcepos)

lazy val sourcepos = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sourcepos"))
  .settings(
    name         := "sourcepos",

    // MUnit
    libraryDependencies += "org.scalameta" %%% "munit" % "1.1.1" % Test,

    // Scala 2 needs scala-reflect
    libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value).filterNot(_ => tlIsScala3.value),

    publishTo := githubPublishTo.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  )
  .jsSettings(
    tlVersionIntroduced := List("2.13", "3").map(_ -> "1.0.1").toMap
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.13", "3").map(_ -> "1.1.0").toMap
  )
