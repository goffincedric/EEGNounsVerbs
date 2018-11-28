package stimuli.model.analysis

/**
  * @author Cédric Goffin
  *         23/11/2018 10:14
  *
  */
class SensorResult(val data: Map[String/* sensorName */, Int /* Index van measurement dat opvalt volgens analyse */ ], val start: Int, val nMeasurements: Int) {

}