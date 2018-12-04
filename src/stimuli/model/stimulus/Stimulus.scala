package stimuli.model.stimulus

import stimuli.model.stimulus.StimulusType.StimulusType

/**
  * @author Cédric Goffin
  *         13/10/2018 12:18
  *
  */
class Stimulus(val stimulusType: StimulusType, val word: String, val measurements: Map[String, Vector[Measurement]]) {
    override def toString: String = {
        f"Woord: $word%s\t Type: $stimulusType%s\t #Metingen: ${measurements.values.flatten.size/measurements.keys.size}%s"
    }

    def toIndentedString: String = {
        f"Woord: $word%-25s\t Type: $stimulusType%-10s\t #Metingen: ${measurements.values.flatten.size/measurements.keys.size}%s"
    }
}
