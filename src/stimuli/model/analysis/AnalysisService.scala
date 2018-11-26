package stimuli.model.analysis

import stimuli.model.result.AnalysisResult
import stimuli.model.stimulus.Stimulus

import scala.Numeric.Implicits._

/**
  * @author Cédric Goffin
  *         19/11/2018 14:26
  *
  */
class AnalysisService {
    def calcBaseLine(measurements: Vector[Double]): Double = mean(measurements)

    def calcStandardDeviation(measurements: Vector[Double]): Double = mean(measurements)

    def analyseData(data: Stimulus, sizeWindowOne: Int, sizeWindowTwo: Int): AnalysisResult = {
        // Calc baseline (avg gegevens)
        val word = data.word
        val measurementResults = data.measurements.map(kvPair => {
            val sensor = kvPair._1

            // Data opdelen in eerste 2 en laatste 2 seconden
            val datasets = splitDataset(kvPair._2.map(m => m.value), 0, 0)
            val measurementDiffs = (calcDifferences(datasets._1), calcDifferences(datasets._2))

            // Gemiddelde berekenen
            val avg = mean(kvPair._2.map(m => m.value))
            val standardDev = stdDev(kvPair._2.map(m => m.value))

            // Normaalverdeling opstellen a.d.h.v. gemiddelde (test uit in geogebra: https://www.geogebra.org/classic#probability)


            // stdDev (standard deviance ( standaard afwijking)) vragen via parameter (


            null
        })


        null
    }

    private def splitDataset(values: Vector[Double], time: Double, index: Int): (Vector[Double], Vector[Double]) = {
        if (time > 2)
            (values.take(index), values.takeRight(values.length - index))
        else
            splitDataset(values, time + values(index), index + 1)
    }

    private def calcDifferences(values: Vector[Double]): Vector[Double] = {
        values.sliding(2).map {
            case Seq(x, y, _*) => y - x
        }.toVector
    }

    private def mean[T: Numeric](xs: Iterable[T]): Double =
        xs.sum.toDouble / xs.size

    private def variance[T: Numeric](xs: Iterable[T]): Double =
        xs.map(_.toDouble).map(a => math.pow(a - mean(xs), 2)).sum / xs.size

    private def stdDev[T: Numeric](xs: Iterable[T]): Double =
        math.sqrt(variance(xs))
}
