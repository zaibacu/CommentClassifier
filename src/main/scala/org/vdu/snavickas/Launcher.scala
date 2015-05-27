package org.vdu.snavickas

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LinearRegressionWithSGD, LabeledPoint}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}

/**
 * Created by sarunasnavickas on 4/28/15.
 */
object Launcher {

  def prepare(data: RDD[String]) = {
    data.map { line =>
      val chars = line.toCharArray
      LabeledPoint(if(chars(0) == '1') 1.0 else 0.0, Vectors.dense(chars.drop(1).map{char => if(char == '1') 1.0 else 0.0}))
    }
  }
  def main(args: Array[String]) = {
    val root = args(0)
    val conf = new SparkConf()
      .setSparkHome("/usr/local/spark")
      //.setMaster("spark://master:7077")
      .setMaster("local[8]")
      .setAppName("CommentTrain: Bayes")
    implicit val sc = new SparkContext(conf)
    sc.addJar("target/scala-2.10/teksto-klasifikatorius-assembly-1.0.jar")
    val data = sc.textFile("%sfeatures.dat".format(root))
    val parsedData = prepare(data)

    val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    //trainBayes(training, test)(root)
    //trainSVM(training, test)(root)
    trainLogistic(training, test)(root)
  }

  def trainBayes(training: RDD[LabeledPoint], test: RDD[LabeledPoint])(root: String) = {
    val model = NaiveBayes.train(training, lambda = 1.0)
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    predictionAndLabel.saveAsTextFile("%sresults_bayes".format(root))
  }

  def trainSVM(training: RDD[LabeledPoint], test: RDD[LabeledPoint])(root: String) = {
    val model = SVMWithSGD.train(training, 100)
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    predictionAndLabel.saveAsTextFile("%sresults_svm".format(root))
  }

  def trainLogistic(training: RDD[LabeledPoint], test: RDD[LabeledPoint])(root: String) = {
    val model = LinearRegressionWithSGD.train(training, 100)
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    predictionAndLabel.saveAsTextFile("%sresults_logistic".format(root))
  }
}
