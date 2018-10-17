package stimulus.model

import stimulus.model.StimulusType.StimulusType

/**
  * @author Cédric Goffin
  *         13/10/2018 12:18
  *
  */
class Stimulus(var stimulusType: StimulusType, var word: String, var measurements: Map[String, Vector[Double]]) {
    override def toString: String = {
        String.format("%-25s\t %-10s\t %s", "\nWoord: " + word, "Type: " + stimulusType, "#Metingen: " + measurements.values.flatten.size)
    }
}
