package com.mariussoutier.sbt

import sbt._
import Keys._
import sbt.plugins.JvmPlugin
import avrohugger.Generator
import avrohugger.filesorter.{AvdlFileSorter, AvscFileSorter}
import avrohugger.format.SpecificRecord

object AvroGenerator extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger: PluginTrigger = noTrigger

  val autoImport = AvroGeneratorKeys

  def generateCaseClasses(generator: Generator,
                          srcDir: File,
                          target: File,
                          log: Logger): Set[java.io.File] = {

    for (inFile <- AvscFileSorter.sortSchemaFiles((srcDir ** "*.avsc").get)) {
      log.info("Compiling AVSC %s".format(inFile))
      generator.fileToFile(inFile, target.getPath)
    }

    for (idl <- AvdlFileSorter.sortSchemaFiles((srcDir ** "*.avdl").get)) {
      log.info("Compiling Avro IDL %s".format(idl))
      generator.fileToFile(idl, target.getPath)
    }

    for (inFile <- (srcDir ** "*.avro").get) {
      log.info("Compiling Avro datafile %s".format(inFile))
      generator.fileToFile(inFile, target.getPath)
    }

    for (protocol <- (srcDir ** "*.avpr").get) {
      log.info("Compiling Avro protocol %s".format(protocol))
      generator.fileToFile(protocol, target.getPath)
    }

    (target ** ("*.java"|"*.scala")).get.toSet
  }

  import AvroGeneratorKeys._

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ Seq(
    sourceDirectory in Avro := (resourceManaged in Compile).value / "avro",
    generateSpecific := {
      generateCaseClasses(
        new Generator(SpecificRecord),
        (sourceDirectory in Avro).value,
        (sourceManaged in Compile).value / "avro",
        streams.value.log
      ).toSeq
    }
  )
}

object AvroGeneratorKeys {

  val Avro = config("avro")

  val generateSpecific = taskKey[Seq[java.io.File]]("Generate specific record case classes")

}
