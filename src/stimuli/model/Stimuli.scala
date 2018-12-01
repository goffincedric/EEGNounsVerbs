package stimuli.model

import java.io.File

import stimuli.model.stimulus.{Measurement, Stimulus, StimulusType}

import scala.io.Source

/**
  * @author Cédric Goffin
  *         17/10/2018 16:14
  *
  */
class Stimuli(path: String, endsWith: String, hardcodedDelayMS: Double = 7.8125) {
    // Fill map with CSV data
    private val _stimuliMap: Map[String, (Vector[Stimulus], Vector[Stimulus])] = readFiles(getListOfCSVFiles, Map())

    def stimuliMap: Map[String, (Vector[Stimulus], Vector[Stimulus])] = _stimuliMap

    def stimuliMapUnsorted: Map[String, Vector[Stimulus]] = stimuliMap.map(kvPair => kvPair._1 -> (kvPair._2._1 ++ kvPair._2._2))

    private def getListOfCSVFiles: List[File] = {
        val d = new File(path) // Get directory with csv files
        if (d.exists && d.isDirectory) {
            // Get all files that have compatible names
            d.listFiles.filter(_.isFile).filter(_.getName.endsWith(endsWith)).toList
        } else {
            // Return empty list of files
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
        val bufferedSource = Source.fromFile(file) // Open reader to file
        val lines = bufferedSource.getLines().toVector // Get lines from file
        bufferedSource.close // Close reader

        // Define contact points array and their respective column index
        val headerLine = lines.head
        val contact_points = Array("AF3", "F7", "F3", "FC5", "T7", "P7", "O1", "O2", "P8", "T8", "FC6", "F4", "F8", "AF4").map(col => (col, getColumnIndex(headerLine, "\t", col)))

        // Convert lines to Stimulus objects and return sorted by StimulusType
        splitNounsVerbs(linesToStimuli(lines.tail, contact_points))
    }

    private def getColumnIndex(headerLine: String, delimiter: String, column: String): Int = {
        headerLine.split(delimiter).map(_.trim).indexOf(column)
    }

    private def linesToStimuli(lines: Vector[String], contact_points: Array[(String, Int)], stimulusLines: Vector[String] = Vector.empty, stimuliVector: Vector[Stimulus] = Vector.empty): Vector[Stimulus] = {
        if (lines.isEmpty) // If no lines left to convert
            stimuliVector // Return vector with stimulus objects
        else {
            // Get first line
            val cols = lines.head.split("\t").map(_.trim)
            if (stimulusLines.nonEmpty && cols(0).equalsIgnoreCase("stimulus")) { // Check if first line contains stimulus word
                // Get first line from stimulusLines
                val firstLine = stimulusLines.head.split("\t").map(_.trim)
                // Convert to stimulus object
                val stimulus = new Stimulus(
                    StimulusType.getType(firstLine(1)),
                    firstLine(1),
                    linesToMeasurements(stimulusLines.tail, contact_points)
                )
                // Recursive call to self with newly converted stimulus
                linesToStimuli(lines.tail, contact_points, Vector(lines.head), stimuliVector :+ stimulus)
            } else {
                // Recursive call to self
                linesToStimuli(lines.tail, contact_points, stimulusLines :+ lines.head, stimuliVector)
            }
        }
    }

    private def linesToMeasurements(lines: Vector[String], contact_points: Array[(String, Int)]): Map[String, Vector[Measurement]] = {
        // Convert lines to measurements and group by contact point
        lines.flatMap(line => lineToMeasurements(line, contact_points))
          .groupBy(_._1) // Group measurements by contact point
          .mapValues(_.map(_._2)) // Map grouped measurements to map
          .map(meting => (meting._1, cleanupData(meting._2, 5))) // Cleanup dirty measurements
    }

    private def lineToMeasurements(line: String, contact_points: Array[(String, Int)]): Map[String, Measurement] = {
        // Convert line to measurement
        val cols = line.split("\t").map(_.trim)
        contact_points.map(cp => cp._1 -> new Measurement(cols(cp._2).toDouble, hardcodedDelayMS)).toMap
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

                // Check if there are 5 elements before current index
                if ((index - 5) < 0) // Get average from previous measurements
                    new Measurement(getAverage(vector.take(n_average - index)), m.delay)
                else // Get average from 5 previous measurements
                    new Measurement(getAverage(vector.take(n_average)), m.delay)
            } else
                m
        }).toVector
    }

    private def getAverage(vector: Vector[Measurement]): Double = {
        vector.map(_.value).sum / vector.size
    }
}
