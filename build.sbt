name := "topwords"

version := "0.4"

libraryDependencies ++= Seq(
  "com.lihaoyi"       %% "mainargs"        % "0.7.8",
  "org.slf4j"         %  "slf4j-api"       % "2.0.13",
  "ch.qos.logback"    %  "logback-classic" % "1.5.6",

  // JUnit 5 API + Engine
  "org.junit.jupiter" % "junit-jupiter-api"    % "5.10.2" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.2" % Test,

  // SBT runner for JUnit 5
  "com.github.sbt.junit" % "jupiter-interface" % "0.17.0" % Test,

  // ScalaTest + ScalaCheck (optional but fine)
  "org.scalatest"     %% "scalatest"       % "3.2.19"   % Test,
  "org.scalacheck"    %% "scalacheck"      % "1.19.0"   % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
)

enablePlugins(JavaAppPackaging)
