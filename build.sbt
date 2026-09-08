// ai-assisted: #1
// why: Claude suggested the standard ScalaFX+sbt bootstrap (OS-classifier javafx modules);
// I kept it as-is since it matches the officially documented ScalaFX 21 setup.
ThisBuild / scalaVersion := "3.3.4"
ThisBuild / version      := "0.1.0"

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unsupported OS for JavaFX classifier")
}

lazy val root = (project in file("."))
  .settings(
    name := "MicrofinanceLoanTracker",
    Compile / scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all"),
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    libraryDependencies ++= Seq("base", "controls", "fxml", "graphics")
      .map(m => "org.openjfx" % s"javafx-$m" % "21.0.2" classifier osName),
    Compile / mainClass := Some("sdg1.microfinance.ui.MainApp"),
    fork := true
  )
