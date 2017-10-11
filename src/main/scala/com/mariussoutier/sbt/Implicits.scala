package com.mariussoutier.sbt

import java.io.File

object Implicits {
  implicit class FilesOps(files: Array[File]) {
    def withSuffix(suffix: String): Array[File] = {
      files.filter(_.getName.endsWith(suffix))
    }
    def sorted(sorter: Traversable[File] => Seq[File]) = sorter(files)
  }
}
