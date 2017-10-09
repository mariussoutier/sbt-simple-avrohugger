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
                          log: Logger,
                          includeFilter: FileFilter,
                          excludeFilter: FileFilter): Set[java.io.File] = {

    def includeFile(file: File): Boolean =
      includeFilter.accept(file) || !excludeFilter.accept(file)

    for (avscFile <- AvscFileSorter.sortSchemaFiles((srcDir ** "*.avsc").get) if includeFile(avscFile)) {
      log.info("Compiling AVSC %s".format(avscFile))
      generator.fileToFile(avscFile, target.getPath)
    }

    for (avdlFile <- AvdlFileSorter.sortSchemaFiles((srcDir ** "*.avdl").get) if includeFile(avdlFile)) {
      log.info("Compiling Avro IDL %s".format(avdlFile))
      generator.fileToFile(avdlFile, target.getPath)
    }

    for (avroFile <- (srcDir ** "*.avro").get if includeFile(avroFile)) {
      log.info("Compiling Avro datafile %s".format(avroFile))
      generator.fileToFile(avroFile, target.getPath)
    }

    for (protocolFile <- (srcDir ** "*.avpr").get if includeFile(protocolFile)) {
      log.info("Compiling Avro protocol %s".format(protocolFile))
      generator.fileToFile(protocolFile, target.getPath)
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
        streams.value.log,
        (includeFilter in Avro).value,
        (excludeFilter in Avro).value
      ).toSeq
    },
    includeFilter in Avro := AllPassFilter,
    excludeFilter in Avro := NothingFilter
  )
}

object AvroGeneratorKeys {

  val Avro = config("avro")

  val generateSpecific = taskKey[Seq[java.io.File]]("Generate specific record case classes")

}
