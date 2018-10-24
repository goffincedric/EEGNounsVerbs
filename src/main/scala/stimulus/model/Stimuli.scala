package stimulus.model

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * @author Cédric Goffin
  *         17/10/2018 16:14
  *
  */
class Stimuli {
    val filesCount = Option(new File("/").list).map(_.count(_.endsWith("_.csv"))).getOrElse(0)
    println(filesCount)


    val stimuliBart: (Vector[Stimulus], Vector[Stimulus]) = readWords("Bart_NounVerb.csv")
    val stimuliBarbara: (Vector[Stimulus], Vector[Stimulus]) = readWords("Barbara_NounVerb.csv")

    def readWords(path: String): (Vector[Stimulus], Vector[Stimulus]) = {
        val nouns = ListBuffer[Stimulus]()
        val verbs = ListBuffer[Stimulus]()

        // Define contactpoints and their respective column index
        val contact_points = Array(("AF3", 3), ("F7", 4), ("F3", 5), ("FC5", 6), ("T7", 7), ("P7", 8), ("O1", 9), ("O2", 10), ("P8", 11), ("T8", 12), ("FC6", 13), ("F4", 14), ("F8", 15), ("AF4", 16))

        // Initialize vars
        val measurements = scala.collection.mutable.Map[String, ListBuffer[Double]]()
        var word = ""

        // Open reader to file
        val bufferedSource = Source.fromResource(path)
        bufferedSource.getLines().drop(1).toStream
          .foreach(l => {
              val cols = l.split("\t").map(_.trim)
              if (cols(0).equalsIgnoreCase("stimulus")) {
                  if (!word.equals("")) {
                      val stimulusType = StimulusType.getType(word)
                      val stimulus = new Stimulus(stimulusType, word, measurements map (meting => (meting._1, meting._2.toVector)) toMap)
                      stimulusType match {
                          case StimulusType.NOUN => nouns += stimulus
                          case StimulusType.VERB => verbs += stimulus
                      }
                  }
                  word = cols(1)
                  measurements.clear()
              } else {
                  for (contact_point <- contact_points) {
                      measurements.get(contact_point._1) match {
                          case Some(xs: ListBuffer[Double]) =>
                              measurements.update(contact_point._1, xs :+ cols(contact_point._2).toDouble)
                          case None =>
                              measurements += contact_point._1 -> ListBuffer[Double](cols(contact_point._2).toDouble)
                      }
                  }

              }
          })

        // Close reader
        bufferedSource.close

        // Return stimuli
        (nouns.toVector, verbs.toVector)
    }


}
