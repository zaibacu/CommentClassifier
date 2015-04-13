package org.vdu.snavickas

import java.io.{File, FileInputStream}

import breeze.numerics.abs
import opennlp.tools
import opennlp.tools.sentdetect.{SentenceModel, SentenceDetectorME}
import opennlp.tools.tokenize.{SimpleTokenizer, TokenizerModel, TokenizerME}
import opennlp.tools.util.Span
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.{ClassificationModel, NaiveBayesModel, SVMModel, SVMWithSGD}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, LabeledPoint}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd._


/**
 * Created by sarunasnavickas on 3/21/15.
 */

trait CommentClass
case object PositiveComment extends CommentClass
case object NegativeComment extends CommentClass
case object UnknownComment extends  CommentClass

case class Comment(text: String, commentClass: CommentClass)

class Knowledge(implicit val sc: SparkContext){
  def createFeatureSet(featureDict: RDD[Feature], features: Vector[Feature]) = {
    featureDict.map {
      feature =>
        (feature, features.exists { x => x ~= feature})
    }
  }
}


case class Word(text: String)
object Word{
  val separators = Set(' ', '.', ',', '!', '?')
  /*val sdStream = new FileInputStream(new File("en-sent.bin"))
  val tokStream = new FileInputStream(new File("en-token.bin"))
  val sentenceDetector = new SentenceDetectorME(new SentenceModel(sdStream))
  val tokenizer = new TokenizerME(new TokenizerModel(tokStream))*/
  def extract(text: String): Stream[Word] = {
    /*val sentences = sentenceDetector.sentDetect(text)
    val tokens = sentences
                    .map { s => tokenizer.tokenize(s) }(*/

    def seq(chars: List[Char]): Stream[Word] = {
      if(chars.length == 0)
        return Stream.Empty

      val wordSeparators = separators.map{ sep => chars.indexOf(sep) }.filterNot{ index => index < 0 }
      if(wordSeparators.size > 0){
        val end = wordSeparators.min
        Word(chars.take(end).mkString.trim) #:: seq(chars.drop(end + 1))
      }
      else{
        Word(chars.mkString.trim) #:: seq(List())
      }

    }
    seq(text.toList)
  }
}

case class Feature(text: String){
  def isEmpty = text.isEmpty
  def ==(other: Feature) = text == other.text
  def !=(other: Feature) = !(this == other)
  def ~=(other: Feature, limit: Int = 2) = {
    this.distance(other) <= limit
  }

  def +(other: Feature): String = {
    if(text.isEmpty)
      other.text
    else if(other.isEmpty)
      text
    else
      "%s %s".format(text, other.text)
  }

  def isShort: Boolean = text.length < 3

  private def distance(c1: Char, c2: Char): Int = {
    abs(c1.toInt - c2.toInt)
  }

  private def distance(other: Feature): Int = {
    Feature.distance(text, other.text)
  }
}

object Feature{
  /*def extract(text: String)
             (implicit convert: (String) => Feature): Vector[Feature] = {
    val words = Word.extract(text)
    words.map{ x => convert(x.text) }.toVector
          .filterNot{ x => x.isEmpty }
  }*/

  def delCost(err: Char): Int = {
    1
  }
  def insCost(err: Char): Int = {
    1
  }
  def subCost(left: Char, right: Char): Int = {
    (left, right) match{
      case ('i', 'j') | ('j', 'i') =>
        0
      case ('u', 'w') | ('w', 'u') =>
        0
      case _ => 1
    }
  }

  private def levenshtein(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length

    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)

    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j

    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j-1)) 0 else subCost(str1(i - 1), str2(j - 1))

      d(i)(j) = min(
        d(i-1)(j  ) + delCost(str1(i - 1)),     // deletion
        d(i  )(j-1) + insCost(str2(j - 1)),     // insertion
        d(i-1)(j-1) + cost   // substitution
      )
    }

    d(lenStr1)(lenStr2)
  }

  private def min(nums: Int*): Int = nums.min

  def distance(left: String, right: String): Int = {
    if(left.equals(right)) 0
    else Feature.levenshtein(left, right)
  }

  def similar(left: String, right: String, limit: Int = 2) = {
    this.distance(left, right) <= limit
  }

  def toNgram(features: Vector[Feature], n: Int = 2): Iterator[Feature] = {
    features
      .filterNot( x => x.isShort)
      .sliding(n)
      .map{ ngram =>
        ngram.foldLeft(Feature("")){ (left, right) => Feature(left + right) }
    }
  }
}
