name := "pluginExample"

version := "0.1"

lazy val plugin = Project(id = "plugin-example", base = file("plugin-example"))
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    ideaBuild                        := "181.4668.68",
    scalaVersion                     := "2.12.2",
    onLoad in Global                 ~= { _.andThen("plugin-example/updateIdea" :: _) },
    assemblyExcludedJars in assembly ++= ideaFullJars.value,
    assemblyOption in assembly       ~= { _.copy(includeScala = false) },
    ideaExternalPlugins += IdeaPlugin
      .Zip("scala-plugin", url("https://plugins.jetbrains.com/plugin/download?rel=true&updateId=45268"))
  )
    
lazy val runner = Project(id = "runner", base = file("plugin-example/target"))
  .settings(
     scalaVersion                := "2.12.2",
     autoScalaLibrary            := false,
     unmanagedJars in Compile    := (ideaMainJars in plugin).value,
     unmanagedJars in Compile    += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
     fork in run                 := true,
     compile in Compile          := ((compile in Compile) dependsOn (assembly in plugin)).value,
     mainClass in (Compile, run) := Some("com.intellij.idea.Main"),
     javaOptions in run ++= Seq(
       "-Xmx2g",
       "-XX:ReservedCodeCacheSize=240m",
       "-XX:MaxPermSize=250m",
       "-XX:+HeapDumpOnOutOfMemoryError",
       "-ea",
       "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005",
       s"-Didea.home=${(ideaBaseDirectory in plugin).value.getPath}",
       "-Didea.is.internal=true",
       "-Didea.debug.mode=true",
       "-Dapple.laf.useScreenMenuBar=true",
       s"-Dplugin.path=${(assemblyOutputPath in (plugin, assembly)).value}",
       s"-Didea.plugins.path=${(ideaBaseDirectory in plugin).value / "externalPlugins"}",
       "-Didea.ProcessCanceledException=disabled"
     )
  )
  