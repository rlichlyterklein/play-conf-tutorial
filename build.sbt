import play.Project._

import net.litola.SassPlugin

name := """playconf"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaEbean,
  "mysql" % "mysql-connector-java" % "5.1.26",
	"org.webjars" %% "webjars-play" % "2.2.0",
	"com.google.inject" % "guice" % "3.0",
	"javax.inject" % "javax.inject" % "1", 
	"org.webjars" % "bootstrap" % "2.3.1")

playJavaSettings ++ SassPlugin.sassSettings
