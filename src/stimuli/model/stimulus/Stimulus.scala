package stimuli.model.stimulus

import stimuli.model.stimulus.StimulusType.StimulusType

/**
  * @author Cédric Goffin
  *         13/10/2018 12:18
  *
  */
class Stimulus(val stimulusType: StimulusType, val word: String, val measurements: Map[String, Vector[Measurement]]) {
    override def toString: String = {
        String.format("%s\t %s\t %s", "Woord: " + word, "Type: " + stimulusType, "#Metingen: " + measurements.values.flatten.size/measurements.keys.size)
    }

    def toIndentedString: String = {
        String.format("%-25s\t %-10s\t %s", "\nWoord: " + word, "Type: " + stimulusType, "#Metingen: " + measurements.values.flatten.size/measurements.keys.size)
    }
}
