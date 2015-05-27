package org.vdu.snavickas.feature

import org.scalatest._
import org.scalameter._
import org.vdu.snavickas.feature.Util.Word

/**
 * Created by sarunasnavickas on 5/1/15.
 */
class FeatureSpec extends FlatSpec with Matchers {
  "A Word util" should "correctly split words" in  {
    val sentence = "A brown fox jumps over the grey pigeon, and then stray dog attacks them"
    val expected = List(Word("A"), Word("BROWN"), Word("FOX"), Word("JUMPS"), Word("OVER"), Word("THE"), Word("GREY"),
      Word("PIGEON"), Word("AND"), Word("THEN"), Word("STRAY"), Word("DOG"), Word("ATTACKS"), Word("THEM"))

    val result = Word.extract(sentence)
    result.toList shouldBe expected
  }

  it should "allow minor grammatical errors" in{
    val result = Word("GRAMMATICAL") ~= Word("GRAMATICAL")
    result shouldBe true
  }
}
