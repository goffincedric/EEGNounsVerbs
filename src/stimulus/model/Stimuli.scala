package stimulus.model

import scala.io.Source
import scala.util.control.Breaks._

/**
  * @author Cédric Goffin
  *         17/10/2018 16:14
  *
  */
class Stimuli {
    // Define contactpoints and their respective column index
    val contact_points = Array(("AF3", 3), ("F7", 4), ("F3", 5), ("FC5", 6), ("T7", 7), ("P7", 8), ("O1", 9), ("O2", 10), ("P8", 11), ("T8", 12), ("FC6", 13), ("F4", 14), ("F8", 15), ("AF4", 16))


    val stimuliBart: (Vector[Stimulus], Vector[Stimulus]) = readWords("Bart_NounVerb.csv")
    val stimuliBarbara: (Vector[Stimulus], Vector[Stimulus]) = readWords("Barbara_NounVerb.csv")

    def readWords(path: String): (Vector[Stimulus], Vector[Stimulus]) = {
        val nouns = List[Stimulus]()
        val verbs = List[Stimulus]()

        // Initialize vars
        val measurements = scala.collection.mutable.Map[String, Vector[Double]]()
        var word = ""

        // Open reader to file
        val bufferedSource = Source.fromResource(path)
        // Get lines from file
        val lines = bufferedSource.getLines().drop(1).toVector
        // Close reader
        bufferedSource.close

        // Lines to Stimulus objects
        val stimuli = linesToStimuli(lines)

        // Split verbs and nouns and return
        splitNounsVerbs(stimuli)
    }

    private def linesToStimuli(lines: Vector[String]): Vector[Stimulus] = {
        var count = 0
        breakable {
            for (line <- lines) {
                val cols = line.split("\t").map(_.trim)
                if (count > 0 && cols(0).equalsIgnoreCase("stimulus")) break
                else count += 1
            }
        }
        val stimulusLines = lines.take(count - 1)
        val firstLine = stimulusLines.head.split("\t").map(_.trim)
        val stimulus = Vector(new Stimulus(
            StimulusType.getType(firstLine(1)),
            firstLine(1),
            linesToMeasurements(stimulusLines.tail)
              map (meting =>
                (meting._1, cleanupData(meting._2, 5))
              )
        ))
        if (lines.length > count) {
            stimulus ++ linesToStimuli(lines.takeRight(lines.length - count))
        } else {
            stimulus
        }
    }

    private def linesToMeasurements(lines: Vector[String]): Map[String, Vector[Double]] = {
        lines.flatMap(lineToMeasurements)
          .groupBy(_._1)
          .mapValues(_.map(_._2))
    }

    private def lineToMeasurements(line: String): Map[String, Double] = {
        val cols = line.split("\t").map(_.trim)
        contact_points.map(cp => cp._1 -> cols(cp._2).toDouble).toMap
    }

    private def splitNounsVerbs(stimuli: Vector[Stimulus]): (Vector[Stimulus], Vector[Stimulus]) = {
        val nouns = stimuli.filter(stimulus => stimulus.stimulusType == StimulusType.NOUN)
        val verbs = stimuli.filter(stimulus => stimulus.stimulusType == StimulusType.VERB)
        (nouns, verbs)
    }

    private def cleanupData(vector: Vector[Double], n_average: Int): Vector[Double] = {
        vector.toStream.map(m => {
            if (m < 3000) {
                val index = vector.indexOf(m)

                // check if there are 5 elements before current index
                if ((index - 5) < 0) {
                    getAverage(vector.take(n_average - index))
                } else {
                    getAverage(vector.take(n_average))
                }
            } else {
                m
            }
        }).toVector
    }

    private def getAverage(vector: Vector[Double]): Double = {
        vector.sum / vector.length
    }
}
