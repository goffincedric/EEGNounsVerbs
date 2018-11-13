package stimuli.view.demo

import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import stimuli.model.Stimuli
import stimuli.model.stimulus.Stimulus

import scala.Numeric.Implicits._

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class DemoPresenter(private val model: Stimuli, private val demoView: DemoView) {
    model.stimuliMap.foreach(addDataPane)

    private def addDataPane(stimuli: (String, (Vector[Stimulus], Vector[Stimulus]))): Unit = {
        // Create the graphs
        val lineChartsMap = (stimuli._2._1 ++ stimuli._2._2).map(stimulus => {
            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis

            // Define the chart
            val lineChart = new LineChart[Number, Number](xAxis, yAxis)
            lineChart.setTitle(stimulus.toString)

            var counter = 0
            for (contact_point <- stimulus.measurements) {
                // Define series
                val series = new XYChart.Series[Number, Number]

                // Populate the series with data
                for (measurement <- contact_point._2) {
                    series.setName(contact_point._1)
                    series.getData.add(new XYChart.Data[Number, Number](counter, measurement.value))
                    counter += 1
                }
                counter = 0

                // Add series to chart
                lineChart.getData.add(series)
            }

            // Return tuple with word and corresponding linechart
            (stimulus.word, lineChart)
        }).toMap

        // Add new tab to view
        val tab = demoView.addTab(stimuli._1, lineChartsMap)


    }


    private def mean[T: Numeric](xs: Iterable[T]): Double =
        xs.sum.toDouble / xs.size

    private def variance[T: Numeric](xs: Iterable[T]): Double = {
        val avg = mean(xs)

        xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
    }

    private def stdDev[T: Numeric](xs: Iterable[T]): Double =
        math.sqrt(variance(xs))
}
