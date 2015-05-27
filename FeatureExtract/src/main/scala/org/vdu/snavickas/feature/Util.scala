package org.vdu.snavickas.feature

import java.io.{FileOutputStream, DataOutputStream}

import scala.io.Source
import scala.math._
/**
 * Created by sarunasnavickas on 4/29/15.
 */
object Util {
  def load(dir: String): Iterator[String] = {
    Source.fromFile(dir).getLines
  }

  object Feature{
    private def getType(score: String): Int = {
      val value = score.toInt
      if(value < 0) 0
      else 1
    }
    def extract(line: String): Feature = {
      val parts = line.split("\t")
      if(parts.length == 2){
        val words = Word.extract(parts(1))
        Feature(getType(parts(0)), words.toList)
      }
      else
        Feature(getType("0"), List())
    }
  }

  case class Feature(cl: Int, words: List[Word])

  /*def wordSplitter(line: String): List[String] = {
    List(line)
  }*/

  def similar(left: String, right: String): Boolean = {
    left.equals(right)
  }

  case class Word(text: String){
    def isEmpty = text.isEmpty
    def ==(other: Word) = text == other.text
    def !=(other: Word) = !(this == other)
    def ~=(other: Word, limit: Int = 2): Boolean = {
      if(abs(text.length - other.text.length) > limit) false
      else this.distance(other) <= limit
    }

    def +(other: Word): String = {
      if(text.isEmpty)
        other.text
      else if(other.isEmpty)
        text
      else
        "%s %s".format(text, other.text)
    }

    def isShort: Boolean = text.length < 3

    private def distance(other: Word): Int = {
      if(text.equals(other.text)) 0
      else if(text.isEmpty) other.text.length
      else if(other.text.isEmpty) text.length
      else
        Word.levenshtein(text, other.text)
    }
  }
  object Word{
    val separators = Set(' ', '.', ',', '!', '?')
    def similar(left: Word, right: Word): Boolean = {
      left ~= right
    }

    def similar(left: String, right: Word): Boolean = {
      right ~= Word(left)
    }

    def extract(text: String): Stream[Word] = {

      def seq(chars: List[Char]): Stream[Word] = {
        if(chars.length == 0)
          return Stream.Empty

        val wordSeparators = separators.map{ sep => chars.indexOf(sep) }.filterNot{ index => index < 0 }
        if(wordSeparators.size > 0){
          val end = wordSeparators.min
          if(chars.take(end).mkString.trim.length > 0)
            Word(chars.take(end).mkString.trim) #:: seq(chars.drop(end + 1))
          else
            seq(chars.drop(end + 1))
        }
        else{
          Word(chars.mkString.trim) #:: seq(List())
        }

      }
      seq(text.toUpperCase.toList)
    }

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

    private def min(nums: Int*): Int = nums.min

    def levenshtein(str1: String, str2: String): Int = {
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
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try
    {
      op(p)
    }
    finally
    {
      p.close()
    }
  }

  def printToBinary(dir: String)(op: DataOutputStream => Unit): Unit ={
    val os = new DataOutputStream(new FileOutputStream(dir))
    try{
      op(os)
    }
    finally{
      os.close
    }
  }

}
