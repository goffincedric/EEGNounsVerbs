package stimuli.model.analysis

import scala.Numeric.Implicits._

/**
  * @author Cédric Goffin
  *         19/11/2018 14:26
  *
  */
class AnalysisService {

    



    private def mean[T: Numeric](xs: Iterable[T]): Double =
        xs.sum.toDouble / xs.size

    private def variance[T: Numeric](xs: Iterable[T]): Double = {
        val avg = mean(xs)

        xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
    }

    private def stdDev[T: Numeric](xs: Iterable[T]): Double =
        math.sqrt(variance(xs))
}
