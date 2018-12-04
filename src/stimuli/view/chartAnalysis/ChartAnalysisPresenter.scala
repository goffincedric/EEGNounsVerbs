package stimuli.view.chartAnalysis

import javafx.collections.ListChangeListener
import javafx.scene.chart.{NumberAxis, XYChart}
import javafx.scene.control.TitledPane
import javafx.scene.layout.VBox
import stimuli.model.Stimuli
import stimuli.model.analysis.result.SensorResult
import stimuli.model.analysis.{AnalysisService, AnalysisType}
import stimuli.utils.customChart.LineChartWithMarkers

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class ChartAnalysisPresenter(private val model: Stimuli, private val name: String, private val word: String, private val chartAnalysisView: ChartAnalysisView) {
    private val analysisService = new AnalysisService
    private val stimulus = model.stimuliMapUnsorted(name).filter(s => s.word.equals(word)).head

    // Split stimulus sensor data
    createSensorGraphs(4, 1)

    // Sensor charts
    val sensorCharts: Vector[LineChartWithMarkers[Number, Number]] = chartAnalysisView.titledPaneContainer.getChildren.toArray.toStream
      .filter(n => n.isInstanceOf[TitledPane]).map(n => n.asInstanceOf[TitledPane])
      .map(tp => tp.getContent).map(n => n.asInstanceOf[VBox])
      .map(v => v.getChildren.filtered(n => n.isInstanceOf[LineChartWithMarkers[Number, Number]]).get(0)).map(n => n.asInstanceOf[LineChartWithMarkers[Number, Number]])
      .toVector

    // Add eventhandlers to tabs
    addEventHandlers()

    private def createSensorGraphs(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
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

            // Return tuple with word and corresponding linechart
            lineChart
        }).toVector

        // Add charts to view
        chartAnalysisView.addCharts(lineChartsMap)
        chartAnalysisView.chcmbSensors.getCheckModel.checkAll()
    }

    private def analyseSensorGraphsHorSlidingWindow(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        val sensorResult = stimulus.measurements.map(sensorMeasurements => {
            val results = analysisService.analyseHorizontalSlidingWindow(sensorMeasurements, 10, sizeWindowOne, 0.95, 100, sizeWindowTwo, 0.97, 4000, 1000)

        }).toVector
    }

    private def analyseSensorGraphsNormalDist(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Modify sensorGraphs
        val sensorResults = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val baseLine = analysisService.calcBaseLine(sensorMeasurements._2.map(m => m.value))
            val sensorResult = analysisService.analyseNormalDist(sensorMeasurements, 10, sizeWindowOne, 0.95, 100, sizeWindowTwo, 0.97)

            // Define the chart
            val sensorChart = sensorCharts.filter(lc => lc.getTitle.equalsIgnoreCase(sensorMeasurements._1)).head

            // Clear charts of previous analyses
            chartAnalysisView.fullLineChart.removeAllMarkers()
            sensorChart.removeAllMarkers()

            // Add valuemarker for baseline
            sensorChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, baseLine))

            // Add valuemarkers for sensorResults
            sensorResult.verticalMarkers.foreach(range => {
                if (range._1 == range._2)
                    sensorChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
                else
                    sensorChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
            })

            // return sensorresult
            sensorResult
        }).toVector

        val unifiedSensorResult = analysisService.mergeSensorResultsResults(sensorResults)
        unifiedSensorResult.data.foreach(range => {
            if (range._1 == range._2)
                chartAnalysisView.fullLineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
            else
                chartAnalysisView.fullLineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
        })
    }

    private def markGraphs(sensorResults: Vector[SensorResult]): Unit = {
        sensorCharts.foreach(lc => {
            // Clear all current markers
            lc.removeAllMarkers()

            // Sensor result
            val sensorResult = sensorResults
              .filter(sr => sr.sensorName.equalsIgnoreCase(lc.getTitle))
              .head

            // Vertical markers
            sensorResult.verticalMarkers.foreach(range => {
                if (range._1 == range._2)
                    lc.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
                else
                    lc.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
            })

            // Horizontal markers
            sensorResult.horizontalMarkers.foreach(marker => {
                lc.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, marker))
            })
        })


        // Clear all current markers
        chartAnalysisView.fullLineChart.removeAllMarkers()

        // Merge SensorResults
        val unifiedSensorResult = analysisService.mergeSensorResultsResults(sensorResults)

        // Vertical markers
        unifiedSensorResult.verticalMarkers.foreach(range => {
            if (range._1 == range._2)
                chartAnalysisView.fullLineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
            else
                chartAnalysisView.fullLineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
        })

        // Horizontal markers
        unifiedSensorResult.horizontalMarkers.foreach(marker => {
            chartAnalysisView.fullLineChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, marker))
        })
    }

    private def chooseAnalysisStrategy(): (Int, Int) => Unit = {
        AnalysisType.withName(chartAnalysisView.cmbAnalysisChoice.getSelectionModel.getSelectedItem) match {
            case AnalysisType.HORIZONTAL_SLIDING_WINDOW =>
                analyseSensorGraphsHorSlidingWindow
            case AnalysisType.VERTICAL_SLIDING_WINDOW => //TODO
                analyseSensorGraphsHorSlidingWindow
            case AnalysisType.NORMAL_DISTRIBUTION =>
                analyseSensorGraphsNormalDist
            case AnalysisType.DIFFERENTIAL_NORMAL_DISTRIBUTION => //TODO
                analyseSensorGraphsHorSlidingWindow
        }
    }

    private def addEventHandlers(): Unit = {
        chartAnalysisView.btnAnalyse.setOnMouseClicked(_ => {
            chooseAnalysisStrategy().apply(4, 1)
        })

        chartAnalysisView.btnClearAllMarkers.setOnMouseClicked(_ => {
            chartAnalysisView.fullLineChart.removeAllMarkers()

            sensorCharts.foreach(lc => {
                lc.removeAllMarkers()
            })
        })

        chartAnalysisView.btnClearHMarkers.setOnMouseClicked(_ => {
            chartAnalysisView.fullLineChart.removeAllHorizontalValueMarkers()

            sensorCharts.foreach(lc => {
                lc.removeAllHorizontalValueMarkers()
            })
        })

        chartAnalysisView.btnClearVMarkers.setOnMouseClicked(_ => {
            chartAnalysisView.fullLineChart.removeAllVerticalValueMarkers()
            chartAnalysisView.fullLineChart.removeAllVerticalRangeMarkers()

            sensorCharts.foreach(lc => {
                lc.removeAllVerticalValueMarkers()
                lc.removeAllVerticalRangeMarkers()
            })
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
