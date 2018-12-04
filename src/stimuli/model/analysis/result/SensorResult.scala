package stimuli.model.analysis.result

/**
  * @author Cédric Goffin
  *         23/11/2018 10:14
  *
  */
class SensorResult(val sensorName: String, val verticalMarkers: Vector[(Int, Int)], val horizontalMarkers: Vector[Double] = Vector.empty[Double], val maxRangeMs: Double = 4000) {
}