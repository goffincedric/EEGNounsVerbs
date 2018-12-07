package stimuli.model.analysis

/**
  * @author Cédric Goffin
  *         03/12/2018 14:34
  *
  */
object AnalysisType extends Enumeration {
    type AnalysisType = Value
    val HORIZONTAL_SLIDING_WINDOW: AnalysisType.Value = Value("Horizontal sliding window")
    val NORMAL_DISTRIBUTION: AnalysisType.Value = Value("Normal distribution")
    val DIFFERENTIAL_NORMAL_DISTRIBUTION: AnalysisType.Value = Value("Differential normal distribution")
}
