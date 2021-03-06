package com.mariussoutier.sbt

import java.io.File

import sbt._
import Keys._
import sbt.plugins.JvmPlugin
import avrohugger.Generator
import avrohugger.filesorter.{AvdlFileSorter, AvscFileSorter}
import avrohugger.format.{Scavro, SpecificRecord, Standard}

import scala.collection.mutable.ListBuffer

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

    val allFiles = listFiles(srcDir, log, includeFilter, excludeFilter, recursive = true)

    for (file <- allFiles) {
      log.info("Compiling %s".format(file))
      generator.fileToFile(file, target.getPath)
    }

    (target ** ("*.java"|"*.scala")).get.toSet
  }

  def listFiles(inputDirectory: File,
                log: Logger,
                includeFilter: FileFilter,
                excludeFilter: FileFilter,
                recursive: Boolean): Seq[File] = {
    import Implicits._

    def includeFile(file: File): Boolean =
      includeFilter.accept(file) && !excludeFilter.accept(file)

    if (inputDirectory.exists()) {
      val directoryFilesAndFolders = inputDirectory.listFiles()
      val schemaFiles = new ListBuffer[File]()

      val directoryFiles = directoryFilesAndFolders.filter(file => includeFile(file))
      schemaFiles ++= directoryFiles.withSuffix(".avsc").sorted(AvscFileSorter.sortSchemaFiles)
      schemaFiles ++= directoryFiles.withSuffix(".avdl").sorted(AvdlFileSorter.sortSchemaFiles)
      schemaFiles ++= directoryFiles.withSuffix(".avpr")
      schemaFiles ++= directoryFiles.withSuffix(".avro")

      if (recursive) {
        schemaFiles ++= directoryFilesAndFolders
          .filter(_.isDirectory)
          .flatMap(listFiles(_, log, includeFilter, excludeFilter, recursive))
      }

      schemaFiles
    } else {
      log.error(s"Directory $inputDirectory doesn't exist")
      Seq.empty
    }
  }

  import AvroGeneratorKeys._

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ Seq(
    sourceDirectory in Avro := (resourceManaged in Compile).value / "avro",
    includeFilter in Avro := AllPassFilter,
    excludeFilter in Avro := NothingFilter,
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
    generateStandard := {
      generateCaseClasses(
        new Generator(Standard),
        (sourceDirectory in Avro).value,
        (sourceManaged in Compile).value / "avro",
        streams.value.log,
        (includeFilter in Avro).value,
        (excludeFilter in Avro).value
      ).toSeq
    },
    generateScavro := {
      generateCaseClasses(
        new Generator(Scavro),
        (sourceDirectory in Avro).value,
        (sourceManaged in Compile).value / "avro",
        streams.value.log,
        (includeFilter in Avro).value,
        (excludeFilter in Avro).value
      ).toSeq
    }
  )
}

object AvroGeneratorKeys {

  val Avro = config("avro")

  val generateSpecific = taskKey[Seq[java.io.File]]("Generate specific record case classes")
  val generateStandard = taskKey[Seq[java.io.File]]("Generate standard record case classes")
  val generateScavro = taskKey[Seq[java.io.File]]("Generate Scavro record case classes")

}
