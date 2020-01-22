import sbtcrossproject.{CrossType, crossProject}
import com.typesafe.sbt.less.Import.LessKeys

/**Resolving a snapshot version. It's going to be slow unless you use `updateOptions := updateOptions.value.withLatestSnapshots(false)` options* */
updateOptions := updateOptions.value.withLatestSnapshots(false)

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalacOptions ++= Seq("-Ypartial-unification"),
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    LessKeys.compress in Assets := true,
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    //pipelineStages := Seq(digest, gzip),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-stream" % "2.5.19",
      "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    ),
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4"),
    javaOptions in run += "-Xms4G -Xmx8G",    //-XX:MaxPermSize=1024M,
  )
  .enablePlugins(SbtWeb, SbtTwirl, WebScalaJSBundlerPlugin, JavaAppPackaging ).
  dependsOn(sharedJvm)


lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    resolvers += ("jitpack" at "https://jitpack.io"),
    npmDependencies in Compile += "snabbdom" -> "0.7.3",
    version in webpack := "4.8.1",
    version in startWebpackDevServer := "3.1.4",
    scalaJSUseMainModuleInitializer := true,
    useYarn := true,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    webpackBundlingMode := BundlingMode.Application,
    scalaJSModuleKind := ModuleKind.CommonJSModule, // configure Scala.js to emit a JavaScript module instead of a top-level script
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js"),
    skip in packageJSDependencies := false,
    libraryDependencies ++= Seq(
      "com.github.outwatch.outwatch" %%% "outwatch" % "master-SNAPSHOT"
    ),
    emitSourceMaps := false,
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.23",
    )
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)
  .jsConfigure(_ enablePlugins ScalaJSBundlerPlugin)
  .jsConfigure(_ enablePlugins SbtWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",     //"2.12.5",
  organization := "example outwatch"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}
