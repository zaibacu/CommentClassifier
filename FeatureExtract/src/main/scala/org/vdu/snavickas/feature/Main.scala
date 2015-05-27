package org.vdu.snavickas.feature

import java.io.File

import Util._

import scalaz.stream._
import scalaz.concurrent.Task
/**
 * Created by sarunasnavickas on 4/29/15.
 */
object Main {
  def main(args: Array[String]) = {
    val dict = Util.load(args(0)).toList.map(_.toUpperCase)
    //val source = Util.load(args(1))
    val sourceDir = args(1)
    def transformFeatures(words: List[Word]) = {
      dict.map{
        item =>
          if(words.exists(_ ~= Word(item))) 1 else 0
      }
    }

    val converter = io.linesR(sourceDir)
                        .map(Feature.extract(_))
                        .map{
                          feature =>
                            (feature.cl, transformFeatures(feature.words))
                        }
                        .map{
                          case(className, features) =>
                            "%s\t%s".format(className, features.mkString(""))
                        }
                        .intersperse("\n")
                        .pipe(text.utf8Encode)
                        .to(io.fileChunkW("features.dat"))
                        .run
    converter.run
  }
}
