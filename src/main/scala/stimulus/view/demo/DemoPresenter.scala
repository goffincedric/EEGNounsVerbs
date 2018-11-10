package stimulus.view.demo

import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control.Tab
import stimulus.model.{Stimuli, Stimulus}

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class DemoPresenter(private val model: Stimuli, private val demoView: DemoView) {
    addDataPane("Bart", model.stimuliBart)
    addDataPane("Barbara", model.stimuliBarbara)

    private def addDataPane(name: String, data: (Vector[Stimulus], Vector[Stimulus])): Unit = {
        // Create the graphs
        val lineCharts = (data._1 ++ data._2).map( stimulus => {
            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis

            // Define the chart
            val lineChart = new LineChart[Number, Number](xAxis, yAxis)
            lineChart.setTitle("Word: " + stimulus.word + " (Type: " + stimulus.stimulusType + ")")

            var counter = 0
            for (contact_point <- stimulus.measurements) {
                // Define series
                val series = new XYChart.Series[Number, Number]

                // Populate the series with data
                for (measurement <- contact_point._2) {
                    series.setName(contact_point._1)
                    series.getData.add(new XYChart.Data[Number, Number](counter, measurement))
                    counter += 1
                }
                counter = 0

                // Add series to chart
                lineChart.getData.add(series)
            }

            lineChart
        })

        demoView.addTab(name, lineCharts)
    }
}
