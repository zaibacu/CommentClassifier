package org.vdu.snavickas.features

import org.tartarus.snowball.ext.lithuanian_stemmer
import org.tartarus.snowball.SnowballStemmer
/**
 * Created by sarunasnavickas on 4/3/15.
 */
object Stemmer {
  val stemmer  = new lithuanian_stemmer()
  def apply(word: String): String = {
    stemmer.setCurrent(word)
    if(stemmer.stem)
      stemmer.getCurrent
    else
      println("Failed to stem")
      word
  }
  /*implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
  val dateMatcher = """([12][09]\d{2})""".r
  val patt1Matcher = """(\w*[aeiou]\w*)(ies|ius|is|ys|as|es)""".r
  val patt2Matcher = """(\w*t)(is|us)""".r
  def apply(word: String): String = {
    if(word.length < 3) word
    else {
      word match {
        case dateMatcher(date) =>
          "{data}"
        case patt1Matcher(stem, _) =>
          //println("Match %s for word %s".format(stem, word))
          stem
        case patt2Matcher(stem, _) =>
          stem
        case x =>
          //println("Didn't match: %s".format(x))
          x
      }
    }
  }*/
}
