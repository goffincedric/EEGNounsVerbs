package stimuli.model.result
import stimuli.model.analysis.SensorResult
import stimuli.model.stimulus.Stimulus
/**
  * @author Cédric Goffin
  *         23/11/2018 09:55
  *
  */
class AnalysisResult(val stimulus: Stimulus, val meanBaseLine: Double, val results: Vector[SensorResult]) {

}
