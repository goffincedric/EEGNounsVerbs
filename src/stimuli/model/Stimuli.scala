package stimuli.model

import java.io.File

import stimuli.model.stimulus.{Measurement, Stimulus, StimulusType}

import scala.io.Source
import scala.util.control.Breaks._

/**
  * @author Cédric Goffin
  *         17/10/2018 16:14
  *
  */
class Stimuli(path: String, endsWith: String) {
    // Define contactpoints and their respective column index
    val contact_points = Array(("AF3", 3), ("F7", 4), ("F3", 5), ("FC5", 6), ("T7", 7), ("P7", 8), ("O1", 9), ("O2", 10), ("P8", 11), ("T8", 12), ("FC6", 13), ("F4", 14), ("F8", 15), ("AF4", 16))
    val timestamp_col = 19

    // Fill map with CSV data
    val stimuliMap: Map[String, (Vector[Stimulus], Vector[Stimulus])] = readFiles(getListOfCSVFiles, Map())

    private def getListOfCSVFiles: List[File] = {
        val d = new File(path)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).filter(_.getName.endsWith(endsWith)).toList
        } else {
            List[File]()
        }
    }

    private def readFiles(files: List[File], stimuliMap: Map[String, (Vector[Stimulus], Vector[Stimulus])]): Map[String, (Vector[Stimulus], Vector[Stimulus])] = {
        // Return map if no files left
        if (files.isEmpty) stimuliMap
        else {
            val file = files.head
            val name = file.getName.substring(0, file.getName.indexOf(endsWith))
            // Convert remaining files recursively by passing current map appended with current file
            readFiles(files.tail, stimuliMap + (name -> readFile(file)))
        }
    }

    private def readFile(file: File): (Vector[Stimulus], Vector[Stimulus]) = {
        // Open reader to file
        val bufferedSource = Source.fromFile(file)
        // Get lines from file
        val lines = bufferedSource.getLines().drop(1).toVector
        // Close reader
        bufferedSource.close

        // Lines to Stimulus objects and return
        splitNounsVerbs(linesToStimuli(lines))
    }

    private def linesToStimuli(lines: Vector[String]): Vector[Stimulus] = {
        // Count lines until next word
        var count = 0
        breakable {
            for (line <- lines) {
                val cols = line.split("\t").map(_.trim)
                if (count > 0 && cols(0).equalsIgnoreCase("stimulus")) break
                else count += 1
            }
        }

        // Get lines for next stimulus
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
            // return stimulus + Stimulus list from remaining lines
            stimulus ++ linesToStimuli(lines.takeRight(lines.length - count))
        } else {
            // return stimulus
            stimulus
        }
    }

    private def linesToMeasurements(lines: Vector[String]): Map[String, Vector[Measurement]] = {
        // Convert lines to measurements and group by contact point
        lines.flatMap(lineToMeasurements)
          .groupBy(_._1)
          .mapValues(_.map(_._2))
    }

    private def lineToMeasurements(line: String): Map[String, Measurement] = {
        // Convert line to measurement
        val cols = line.split("\t").map(_.trim)
        contact_points.map(cp => cp._1 -> new Measurement(cols(cp._2).toDouble, cols(timestamp_col).toDouble)).toMap
    }

    private def splitNounsVerbs(stimuli: Vector[Stimulus]): (Vector[Stimulus], Vector[Stimulus]) = {
        val nouns = stimuli.filter(stimulus => stimulus.stimulusType == StimulusType.NOUN)
        val verbs = stimuli.filter(stimulus => stimulus.stimulusType == StimulusType.VERB)
        (nouns, verbs)
    }

    private def cleanupData(vector: Vector[Measurement], n_average: Int): Vector[Measurement] = {
        vector.toStream.map(m => {
            if (m.value < 3000) {
                val index = vector.indexOf(m)

                // check if there are 5 elements before current index
                if ((index - 5) < 0) {
                    // Get average from previous measurements
                    new Measurement(getAverage(vector.take(n_average - index)), m.delay)
                } else {

                    // Get average from 5 previous measurements
                    new Measurement(getAverage(vector.take(n_average)), m.delay)
                }
            } else {
                m
            }
        }).toVector
    }

    private def getAverage(vector: Vector[Measurement]): Double = {
        vector.map(_.value).sum.toDouble / vector.size
    }
}
