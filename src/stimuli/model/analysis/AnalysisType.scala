package stimuli.model.analysis

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         03/12/2018 14:34
  *
  */
object AnalysisType extends Enumeration {
    type AnalysisType = Value
    val HORIZONTAL_SLIDING_WINDOW: AnalysisType.Value = Value("Horizontal sliding window")
    val NORMAL_DISTRIBUTION: AnalysisType.Value = Value("Normal distribution")
}
