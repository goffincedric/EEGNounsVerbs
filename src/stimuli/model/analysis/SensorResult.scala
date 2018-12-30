package stimuli.model.analysis

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         23/11/2018 10:14
  *
  */
class SensorResult(val sensorName: String, val verticalMarkers: Vector[(Int, Int)], val horizontalMarkers: Vector[Double] = Vector.empty[Double], val maxRangeMs: Double = 4000, val description: String = "") {
}