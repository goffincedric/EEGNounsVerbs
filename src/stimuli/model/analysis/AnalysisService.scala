package stimuli.model.analysis

import org.apache.commons.math3.distribution.NormalDistribution
import stimuli.model.result.AnalysisResult
import stimuli.model.stimulus.{Measurement, Stimulus}

import scala.Numeric.Implicits._

/**
  * @author Cédric Goffin
  *         19/11/2018 14:26
  *
  */
class AnalysisService {
    def calcBaseLine(measurements: Vector[Double]): Double = calcMean(measurements)

    def calcStandardDeviation(measurements: Vector[Double]): Double = calcStdDev(measurements)

    def analyseData(data: Stimulus, sizeWindowOne: Int, probabilityTriggerOne: Double, sizeWindowTwo: Int, probabilityTriggerTwo: Double): AnalysisResult = {
        // Calc baseline (avg gegevens)
        val word = data.word
        val measurementResults = data.measurements.map(kvPair => {
            val sensor = kvPair._1

            // Data opdelen in eerste 2 en laatste 2 seconden
            val datasets = splitDataset(kvPair._2, 0, 0)

            // Get differences in following measurements
            val measurementDiffs = (calcDifferences(datasets._1), calcDifferences(datasets._2))

            val results = analyseDataFramesNormalDist()

            // stdDev (standard deviance ( standaard afwijking)) vragen via parameter (


            null
        })


        null
    }

    private def analyseDataFramesNormalDist(data: Vector[Measurement], windowsMs: Double, windowSize: Int, probabilityTrigger: Double, resultsMeasurementList: Vector[Measurement]): Vector[Measurement] = {
        if (data.length < windowSize) resultsMeasurementList
        val window = getFirstWindow(data, windowsMs, windowSize)

        // Gemiddelde berekenen
        val mean = calcMean(window.map(m => m.value))
        val stdDev = calcStdDev(window.map(m => m.value))

        // Normaalverdeling opstellen a.d.h.v. gemiddelde (test uit in geogebra: https://www.geogebra.org/classic#probability)
        val normalDist = new NormalDistribution(mean, stdDev)

        // stdDev * 2 = 95% van alle waarden => waarde groter dan stdDev*2 is dus opmerkelijk
        val normalProbability = normalDist.probability(mean - (stdDev * 2), mean + (stdDev * 2))
        // Todo: probabilityTrigger gebruiken? als probability < probabilityTrigger

        val probability = normalDist.probability(window.last.value)
        analyseDataFramesNormalDist(
            data.tail,
            windowsMs,
            windowSize,
            probabilityTrigger,
            if (probability < normalProbability)
                resultsMeasurementList :+ data.last
            else
                resultsMeasurementList
        )
    }

    private def getFirstWindow(data: Vector[Measurement], windowMs: Double, windowSize: Int = 1, count: Int = 0): Vector[Measurement] = {
        if (data.take(count).map(m => m.delay).sum > (windowMs * windowSize)) data.take(count - 1)
        else {
            getFirstWindow(data, windowMs, windowSize, count + 1)
        }
    }

    private def splitDataset(values: Vector[Measurement], time: Double, index: Int): (Vector[Measurement], Vector[Measurement]) = {
        if (time > 2)
            (values.take(index), values.takeRight(values.length - index))
        else
            splitDataset(values, time + values(index).delay, index + 1)
    }

    private def calcDifferences(values: Vector[Measurement]): Vector[Double] = {
        values.sliding(2).map {
            case Seq(x, y, _*) => y.value - x.value
        }.toVector
    }

    private def calcMean[T: Numeric](xs: Iterable[T]): Double =
        xs.sum.toDouble / xs.size

    private def calcVariance[T: Numeric](xs: Iterable[T]): Double =
        xs.map(_.toDouble).map(a => math.pow(a - calcMean(xs), 2)).sum / xs.size

    private def calcStdDev[T: Numeric](xs: Iterable[T]): Double =
        math.sqrt(calcVariance(xs))
}
