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
        chartAnalysisView.fullLineChart.setTitle(stimulus.toString)

        stimulus.measurements.foreach(cp => {
            // Define series
            val series = new XYChart.Series[Number, Number]

            // Populate the series with data
            for (measurement <- cp._2) {
                series.setName(cp._1)
                series.getData.add(new XYChart.Data[Number, Number](cp._2.indexOf(measurement), measurement.value))
            }

            // Add series to chart
            chartAnalysisView.fullLineChart.getData.add(series)

            // Set line style
            series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")
        })

        // Create the graphs
        val lineChartsMap = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val baseLine = analysisService.calcBaseLine(sensorMeasurements._2.map(m => m.value))
            val sensorResult = analysisService.analyseSensorDataNormalDist(sensorMeasurements, 10, 4, 0.95, 100, 1, 0.97)

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

            sensorMeasurements._2.foreach(measurement => {
                // Populate the series with data
                series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement), measurement.value))
            })
            lineChart.getData.add(series)

            // Set line style
            series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")

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
        chartAnalysisView.btnClearAllMarkers.setOnMouseClicked(_ => {
            chartAnalysisView.fullLineChart.removeAllHorizontalValueMarkers()
            chartAnalysisView.fullLineChart.removeAllVerticalValueMarkers()
            chartAnalysisView.fullLineChart.removeAllVerticalRangeMarkers()
        })

        chartAnalysisView.btnClearHMarkers.setOnMouseClicked(_ => chartAnalysisView.fullLineChart.removeAllHorizontalValueMarkers())

        chartAnalysisView.btnClearVMarkers.setOnMouseClicked(_ => {
            chartAnalysisView.fullLineChart.removeAllVerticalValueMarkers()
            chartAnalysisView.fullLineChart.removeAllVerticalRangeMarkers()
        })

        chartAnalysisView.chcmbSensors.getCheckModel.getCheckedItems.addListener(new ListChangeListener[String] {
            override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
                while (c.next()) {
                    if (c.wasAdded()) {
                        val tp = chartAnalysisView.titledPaneContainer.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.split("Sensor: ")(1).equalsIgnoreCase(c.getAddedSubList.get(0)))
                          .get(0)
                        tp.setVisible(true)
                        tp.setManaged(true)

                        val seriesNode = chartAnalysisView.fullLineChart.getData
                          .filtered(serie => serie.getName.equalsIgnoreCase(c.getAddedSubList.get(0)))
                          .get(0).getNode
                        seriesNode.setVisible(true)
                        seriesNode.setManaged(true)
                    } else if (c.wasRemoved()) {
                        val tp = chartAnalysisView.titledPaneContainer.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.split("Sensor: ")(1).equalsIgnoreCase(c.getRemoved.get(0)))
                          .get(0)
                        tp.setVisible(false)
                        tp.setManaged(false)

                        val seriesNode = chartAnalysisView.fullLineChart.getData
                          .filtered(serie => serie.getName.equalsIgnoreCase(c.getRemoved.get(0)))
                          .get(0).getNode
                        seriesNode.setVisible(false)
                        seriesNode.setManaged(false)
                    }
                }
            }
        })
    }


}
