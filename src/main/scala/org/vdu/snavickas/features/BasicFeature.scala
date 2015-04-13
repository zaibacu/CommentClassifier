package org.vdu.snavickas.features

import org.vdu.snavickas.{Word, Feature}

/**
 * Created by sarunasnavickas on 3/22/15.
 */
object BasicFeature {
  val toReplace: CharSequence = "ąčęėįšųūž"
  val replace: CharSequence = "aceeisuuz"
  implicit def convert(x: String): Feature = {
    Feature(
      x
        .toLowerCase()
        .replace(toReplace, replace)
    )
  }

  implicit def extract(text: String): Vector[Feature] = {
    val words = Word.extract(text)
    val filtered = words.map{ x => convert(x.text) }.toVector
      .filterNot{ x => x.isEmpty }
    //Feature.toNgram(filtered, 2).toVector
    filtered
  }
}
