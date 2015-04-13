package org.vdu.snavickas

import org.apache.spark.SparkContext

import scala.io._

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Created by sarunasnavickas on 3/20/15.
 */

object Util {
  def loadDictFromFS(fileName: String)
                    (extract: (String) => Vector[Feature]): Set[Feature] = {
    val dict = Source.fromURL(getClass.getResource(fileName)).getLines.mkString(" ")
    val result = extract(dict)
    result.toSet
  }

  def loadDictFromHDFS(fileName: String)
                      (implicit sc: SparkContext, extract: (String) => Vector[Feature]) = {
    sc.textFile(fileName).map{ line => extract(line)(0) }
  }

  def loadCommentsFromHDFS(fileName: String)
                        (implicit sc: SparkContext, extract: (String) => Vector[Feature]) = {

    def parseComment(line: String): Comment = {
      val splits = line.split("\t")
      val balance = splits(0).trim.toInt
      val text = splits(1)
      if(balance >= 0)
        Comment(text, PositiveComment)
      else
        Comment(text, NegativeComment)
    }

    val collection = sc.textFile(fileName)
    for{
      col <- collection
    }
    yield(parseComment(col))
  }

  def loadCommentsFromFS(fileName: String): List[Comment] = {
    val json = parse(Source.fromURL(getClass.getResource(fileName)).getLines.mkString)
    loadComments(json)
  }

  def loadComments(json: JValue): List[Comment] = {
    def getCommentClass(obj: Map[String, JValue]): CommentClass = {
      obj.getOrElse("balance", JString("")) match {
        case JString(text) =>
          val value = text.trim.toInt
          if(value >= 0)
            PositiveComment
          else
            NegativeComment
        case _ => UnknownComment
      }
    }

    json.children.map{
      case JObject(map) =>
        map.toMap.getOrElse("comment", JString("")) match {
          case JString(text) =>
            Comment(text, getCommentClass(map.toMap))
          case _ =>
            Comment("", UnknownComment)
        }
      case _ =>
        Comment("", UnknownComment)
    }
  }

  def confusionMatrix(posCorrect: Long, posIncorrect: Long, negCorrect: Long, negIncorrect: Long) = {
    println("Confusion matrix:\n\tCorrect\tIncorrect")
    println("Positive\t%d\t%d".format(posCorrect, posIncorrect))
    println("Negative\t%d\t%d".format(negCorrect, negIncorrect))
  }

  def timer[T](name: String)(body:  => T): T = {
    val start = System.nanoTime()
    val result = body
    val end = System.nanoTime()
    println("Part: %s took %d ms".format(name, (end - start)/1000))
    result
  }
}
