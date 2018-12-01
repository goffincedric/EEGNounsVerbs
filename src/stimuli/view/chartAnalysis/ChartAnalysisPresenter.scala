package stimuli.view.chartAnalysis

import javafx.collections.ListChangeListener
import javafx.scene.chart.{NumberAxis, XYChart}
import javafx.scene.control.TitledPane
import stimuli.model.Stimuli
import stimuli.model.analysis.AnalysisService
import stimuli.model.stimulus.Stimulus
import stimuli.utils.customChart.LineChartWithMarkers

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class ChartAnalysisPresenter(private val model: Stimuli, private val name: String, private val word: String, private val chartAnalysisView: ChartAnalysisView) {
    private val analysisService = new AnalysisService
    private val stimulus = model.stimuliMapUnsorted(name).filter(s => s.word.equals(word)).head

    // Analyse stimulus
    createSensorGraphs(stimulus, 1, 1)

    // Add eventhandlers to tabs
    addEventHandlers()

    private def createSensorGraphs(stimulus: Stimulus, sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Create the graphs
        val lineChartsMap = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val baseLine = analysisService.calcBaseLine(sensorMeasurements._2.map(m => m.value))
            val sensorResult = analysisService.analyseSensorData(sensorMeasurements, 10, 4, 0.95, 100, 1, 0.97)

            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis
            yAxis.setForceZeroInRange(false)

            // Define the chart
            val lineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)
            lineChart.setTitle(sensorMeasurements._1) // Sensor name
            lineChart.setCreateSymbols(false)

            // Define series
            val series = new XYChart.Series[Number, Number]
            series.setName(sensorMeasurements._1)

            var counter = 0
            sensorMeasurements._2.foreach(measurement => {
                // Populate the series with data
                series.getData.add(new XYChart.Data[Number, Number](counter, measurement.value))
                counter += 1
            })
            lineChart.getData.add(series)

            // Add valuemarker for baseline
            lineChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, baseLine))

            // Add valuemarkers for sensorResults
            sensorResult.data.foreach(range => {
                if (range._1 == range._2)
                    lineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
                else
                    lineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
            })

            // Return tuple with word and corresponding linechart
            lineChart
        }).toVector

        // Add charts to view
        chartAnalysisView.addCharts(lineChartsMap)
        chartAnalysisView.chcmbSensors.getCheckModel.checkAll()
    }

    private def addEventHandlers(): Unit = {
        chartAnalysisView.chcmbSensors.getCheckModel.getCheckedItems.addListener(new ListChangeListener[String] {
            override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
                while (c.next()) {
                    if (c.wasAdded()) {
                        val tp = chartAnalysisView.titledPaneContainer.getChildren.filtered(n => n.asInstanceOf[TitledPane].getText.split("Sensor: ")(1).equalsIgnoreCase(c.getAddedSubList.get(0))).get(0)
                        tp.setVisible(true)
                        tp.setManaged(true)
                    } else if (c.wasRemoved()) {
                        val tp = chartAnalysisView.titledPaneContainer.getChildren.filtered(n => n.asInstanceOf[TitledPane].getText.split("Sensor: ")(1).equalsIgnoreCase(c.getRemoved.get(0))).get(0)
                        tp.setVisible(false)
                        tp.setManaged(false)
                    }
                }
            }
        })
    }
}
