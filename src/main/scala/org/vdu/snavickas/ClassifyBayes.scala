package org.vdu.snavickas

import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

import scala.util.Random

/**
 * Created by sarunasnavickas on 4/11/15.
 */
object ClassifyBayes {
  import features.BasicFeature._
  implicit val sc = new SparkContext("spark://master:7077", "CommentClassify: Bayes")
  val knowledge = new Knowledge()
  val dict = Util.loadDictFromHDFS("hdfs://master:9000/data/dict.txt").distinct
  val comments = Util.loadCommentsFromHDFS("hdfs://master:9000/data/comments_lite.txt")


  def main(args: Array[String]): Unit ={
    val features = for{
      comment <- comments
    }yield{
      comment.commentClass match{
        case PositiveComment =>
          LabeledPoint(1.0,
            Vectors.dense(
                    knowledge.createFeatureSet(dict, extract(comment.text))
                              .map{ x => if(x._2) 1.0 else 0.0 }.collect
                    )
          )
        case NegativeComment =>
          LabeledPoint(0.0,
            Vectors.dense(
                    knowledge.createFeatureSet(dict, extract(comment.text))
                      .map{ x => if(x._2) 1.0 else 0.0 }.collect
                    )
          )
      }



    }
    val count = features.count
    val splits = features.randomSplit(Array(0.6, 0.4))

    val training = splits(0)
    val test = splits(1)

    val model = NaiveBayes.train(training, 100)

    val testClasses = test
      .map {
      x =>
        (x.label, model.predict(x.features))
    }

    val confusionPositive = testClasses.filter{
      case (expected, classification) =>
        expected == 1.0
    }

    val confusionNegative = testClasses.filter{
      case (expected, classification) =>
        expected == 0.0
    }

    confusionPositive.saveAsTextFile("hdfs://master:9000/data/results/positive.txt")
    confusionNegative.saveAsTextFile("hdfs://master:9000/data/results/negative.txt")

    val positiveCorrect = confusionPositive.filter{ case (_, prediction) => prediction >= 0.5}.count
    val positiveIncorrect = confusionPositive.count - positiveCorrect

    val negativeCorrect = confusionNegative.filter{ case (_, prediction) => prediction < 0.5}.count
    val negativeIncorrect = confusionNegative.count - negativeCorrect
  }
}
