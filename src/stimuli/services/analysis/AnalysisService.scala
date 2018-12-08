package stimuli.services.analysis

import org.apache.commons.math3.distribution.NormalDistribution
import stimuli.model.analysis.SensorResult
import stimuli.model.stimulus.Measurement

import scala.Numeric.Implicits._

/**
  * @author Cédric Goffin
  *         19/11/2018 14:26
  *
  */
class AnalysisService {
    def calcBaseLine(measurements: Vector[Double]): Double = calcMean(measurements)

    def calcStandardDeviation(measurements: Vector[Double]): Double = calcStdDev(measurements)

    def mergeSensorResultsResults(sensorResults: Iterable[SensorResult]): SensorResult = {
        // Merge all SensorResult indexes
        val indexes = sensorResults
          .flatMap(sr => sr.verticalMarkers)
          .flatMap(indexes => {
              if (indexes._1 == indexes._2) Vector(indexes._1)
              else (indexes._1 to indexes._2).toVector
          }).toStream
          .sorted
          .distinct
          .toVector

        // Merge indexes
        val mergedIndexes = mergeRemarkableMeasurementIndexes(indexes)

        // Return unified SensorResult
        new SensorResult("Overview", mergedIndexes)
    }

    def analyseHorizontalSlidingWindow(sensorMeasurements: (String, Vector[Measurement]), splitPointMs: Double,windowsSizeMsOne: Double, sizeWindowOne: Int, probabilityTriggerOne: Double, windowsSizeMsTwo: Double, sizeWindowTwo: Int, probabilityTriggerTwo: Double, maxRangeMS: Double = 4000, rangeStepMs: Double = 1000, counter: Int = 1): Vector[SensorResult] = {
        // Recursion check
        if (counter * rangeStepMs > maxRangeMS) {
            // Return sensorResult vector
            return Vector.empty
        }

        // Get range from sensorMeasurements
        val range = getFirstWindow(sensorMeasurements._2, counter * rangeStepMs)
        // Split dataset in first and last part
        val datasets = splitDataset(range, splitPointMs)

        // Calc mean and stdDev
        val mean = calcMean(range.map(m => m.value))
        val stdDev = calcStdDev(range.map(m => m.value))

        // Get noticeable indexes from range
        val remarkableHorWindowIndexes = if (datasets._2.isEmpty) {
            analyseDataFramesHorWindow(datasets._1, windowsSizeMsOne, sizeWindowOne, probabilityTriggerOne, mean, stdDev, 0)
        } else {
            analyseDataFramesHorWindow(datasets._1, windowsSizeMsOne, sizeWindowOne, probabilityTriggerOne, mean, stdDev, 0) ++ analyseDataFramesHorWindow(datasets._2, windowsSizeMsTwo, sizeWindowTwo, probabilityTriggerTwo, mean, stdDev, datasets._1.size)
        }

        val remarkableIndexes = mergeRemarkableMeasurementIndexes(remarkableHorWindowIndexes.distinct.sorted)

        // Return new SensorResult with recursion vector
        Vector(new SensorResult(sensorMeasurements._1, remarkableIndexes, Vector(mean), counter * rangeStepMs, f"Mean: $mean%.3f; standard deviation: $stdDev%.7f")) ++ analyseHorizontalSlidingWindow(sensorMeasurements, splitPointMs,windowsSizeMsOne, sizeWindowOne, probabilityTriggerOne, windowsSizeMsTwo, sizeWindowTwo, probabilityTriggerTwo, maxRangeMS, rangeStepMs, counter + 1)
    }

    private def analyseDataFramesHorWindow(data: Vector[Measurement], windowsMs: Double, windowSize: Int, probabilityTrigger: Double, mean: Double, stdDev: Double, indexOffset: Int, resultsMeasurementList: Vector[Measurement] = Vector(), origData: Vector[Measurement] = Vector()): Vector[Int] = {
        if (data.length < windowSize) {
            resultsMeasurementList.map(m => origData.indexOf(m) + indexOffset)
        } else {
            val window = getFirstWindow(data, windowsMs, windowSize)

            val windowMean = calcMean(window.map(m => m.value))
            val windowStdDev = calcStdDev(window.map(m => m.value))

            val measurements = (if (windowMean > mean) {
                window.filter(m => m.value > mean)
            } else {
                Vector.empty[Measurement]
            }) ++ (if (windowStdDev > stdDev) {
                window
            } else {
                Vector.empty[Measurement]
            })

            analyseDataFramesHorWindow(
                data.tail,
                windowsMs,
                windowSize,
                probabilityTrigger,
                mean,
                stdDev,
                indexOffset,
                resultsMeasurementList ++ measurements,
                if (origData.nonEmpty)
                    origData
                else
                    data
            )
        }
    }

    def analyseNormalDist(sensorMeasurements: (String, Vector[Measurement]), splitPointMs: Double , windowsSizeMsOne: Double, sizeWindowOne: Int, probabilityTriggerOne: Double, windowsSizeMsTwo: Double, sizeWindowTwo: Int, probabilityTriggerTwo: Double): SensorResult = {
        // Data opdelen in eerste 2 en laatste 2 seconden
        val datasets = splitDataset(sensorMeasurements._2, splitPointMs)

        // Gemiddelde berekenen
        val mean = calcMean(sensorMeasurements._2.map(m => m.value))
        val stdDev = calcStdDev(sensorMeasurements._2.map(m => m.value))

        // Set up normal distribution
        val normalDist = new NormalDistribution(mean, stdDev)

        // Get differences in following measurements
        val measurementDiffs = (calcDifferences(datasets._1), calcDifferences(datasets._2))

        val remarkableNormalDistIndexes = analyseDataFramesNormalDist(datasets._1, normalDist, windowsSizeMsOne, sizeWindowOne, probabilityTriggerOne) ++ analyseDataFramesNormalDist(datasets._2, normalDist, windowsSizeMsTwo, sizeWindowTwo, probabilityTriggerTwo, indexOffset = datasets._1.size)

        // stdDev (standard deviance ( standaard afwijking)) vragen via parameter
        val remarkableIndexes = mergeRemarkableMeasurementIndexes(remarkableNormalDistIndexes.distinct.sorted)

        new SensorResult(sensorMeasurements._1, remarkableIndexes)
    }

    private def analyseDataFramesNormalDist(data: Vector[Measurement], normalDistribution: NormalDistribution, windowsMs: Double, windowSize: Int, probabilityTrigger: Double, indexOffset: Int = 0, resultsMeasurementList: Vector[Measurement] = Vector(), origData: Vector[Measurement] = Vector()): Vector[Int] = {
        if (data.length < windowSize) {
            resultsMeasurementList.map(m => origData.indexOf(m) + indexOffset)
        } else {
            val window = getFirstWindow(data, windowsMs, windowSize)

            // Normaalverdeling opstellen a.d.h.v. gemiddelde (test uit in geogebra: https://www.geogebra.org/classic#probability)
            if (normalDistribution.getStandardDeviation > 0) {
                // stdDev * 2 = 95% van alle waarden => waarde groter dan stdDev*2 is dus opmerkelijk
                //                val normalProbability = normalDist.probability(mean - (stdDev * 2), mean + (stdDev * 2))
                // Todo: probabilityTrigger gebruiken? als probability < probabilityTrigger
                //                val probability =
                //                    if (window.last.value < mean)
                //                        normalDist.probability(normalDist.getSupportLowerBound, window.last.value)
                //                    else
                //                        normalDist.probability(window.last.value, normalDist.getSupportUpperBound)

                val indexes = window.map(m => {
                    if (m.value < normalDistribution.getMean)
                        (normalDistribution.probability(normalDistribution.getSupportLowerBound, m.value), m)
                    else
                        (normalDistribution.probability(m.value, normalDistribution.getSupportUpperBound), m)
                }).filter(m => m._1 < (1 - probabilityTrigger))

                analyseDataFramesNormalDist(
                    data.tail,
                    normalDistribution,
                    windowsMs,
                    windowSize,
                    probabilityTrigger,
                    indexOffset,
                    resultsMeasurementList ++ indexes.map(m => m._2),
                    if (origData.nonEmpty)
                        origData
                    else
                        data
                )
            } else {
                analyseDataFramesNormalDist(
                    data.tail,
                    normalDistribution,
                    windowsMs,
                    windowSize,
                    probabilityTrigger,
                    indexOffset,
                    resultsMeasurementList,
                    if (origData.nonEmpty)
                        origData
                    else
                        data
                )
            }
        }
    }

    private def splitDataset(measurements: Vector[Measurement], splitTimeMs: Double, time: Double = 0, index: Int = 0): (Vector[Measurement], Vector[Measurement]) = {
        if (index == measurements.length || time + measurements(index).delay > splitTimeMs)
            (measurements.take(index), measurements.takeRight(measurements.length - index))
        else
            splitDataset(measurements, splitTimeMs, time + measurements(index).delay, index + 1)
    }

    def getFirstWindow(data: Vector[Measurement], windowMs: Double, windowSize: Int = 1, count: Int = 0): Vector[Measurement] = {
        if (count > data.length || data.take(count).map(m => m.delay).sum > (windowMs * windowSize)) data.take(count - 1)
        else {
            getFirstWindow(data, windowMs, windowSize, count + 1)
        }
    }

    private def mergeRemarkableMeasurementIndexes(measurementIndexes: Vector[Int]): Vector[(Int, Int)] = {
        if (measurementIndexes.length == 1) {
            Vector((measurementIndexes.head, measurementIndexes.last))
        } else {
            val consecutive = measurementIndexes.sliding(2).takeWhile(p => p(0) + 1 == p(1)).flatten.toVector

            val consecutiveIndex =
                if (consecutive.isEmpty)
                    (measurementIndexes.head, measurementIndexes.head)
                else
                    (consecutive.head, consecutive.last)

            if (measurementIndexes.last == consecutiveIndex._2) {
                Vector(consecutiveIndex)
            } else {
                Vector(consecutiveIndex) ++ mergeRemarkableMeasurementIndexes(measurementIndexes.takeRight(measurementIndexes.length - (measurementIndexes.indexOf(consecutiveIndex._2) + 1)))
            }
        }
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
