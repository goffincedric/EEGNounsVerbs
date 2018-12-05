package stimuli.view.chartAnalysis

import javafx.collections.ListChangeListener
import javafx.scene.chart.{NumberAxis, XYChart}
import javafx.scene.control.TitledPane
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

    // Add eventhandlers to tabs
    addEventHandlers()

    private def createSensorGraphs(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Full Linechart
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

        // Create sliding window graphs
        val lineChartsSlidingWindowMap = stimulus.measurements.map(sensorMeasurements => {
            val linecharts = Vector(1000, 2000, 3000, 4000).map(range => {
                // Define the axis
                val xAxis = new NumberAxis
                xAxis.setLabel("Measurement")
                val yAxis = new NumberAxis
                yAxis.setForceZeroInRange(false)

                // Define the chart
                val lineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)
                lineChart.setTitle(sensorMeasurements._1 + "; Range: 0ms -> " + range + "ms") // Sensor name
                lineChart.setCreateSymbols(false)

                // Define series
                val series = new XYChart.Series[Number, Number]
                series.setName(sensorMeasurements._1)

                analysisService.getFirstWindow(sensorMeasurements._2, range).foreach(measurement => {
                    // Populate the series with data
                    series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement), measurement.value))
                })
                lineChart.getData.add(series)

                // Set line style
                series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")

                // Return linechart
                lineChart
            })

            // Return entry with sensorname and corresponding linechart
            sensorMeasurements._1 -> linecharts
        })
        // Add charts to container
        chartAnalysisView.addCharts(lineChartsSlidingWindowMap, "Sensor range charts", chartAnalysisView.titledPaneContainerSlidingWindow)

        // Create Normal distribution graphs
        val lineChartsNormalDistMap = stimulus.measurements.map(sensorMeasurements => {
            // Define the axis
            val xAxis = new NumberAxis
            xAxis.setLabel("Measurement")
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

            // Return entry with sensorname and corresponding linechart
            sensorMeasurements._1 -> Vector(lineChart)
        })
        // Add charts to container
        chartAnalysisView.addCharts(lineChartsNormalDistMap, "Sensor detail charts", chartAnalysisView.titledPaneContainerNormal)

        // Add to checkComboBox
        stimulus.measurements.keys.foreach(chartAnalysisView.chcmbSensors.getItems.add)
        chartAnalysisView.chcmbSensors.getCheckModel.checkAll()
    }

    private def analyseSensorGraphsHorSlidingWindow(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Map of sensorResults and Charts
        val sensorResultsMap = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val results = analysisService.analyseHorizontalSlidingWindow(sensorMeasurements, 10, sizeWindowOne, 0.95, 100, sizeWindowTwo, 0.97)

            // Create sliding window graphs
            val linecharts = results.map(result => {
                // Define the axis
                val xAxis = new NumberAxis
                xAxis.setLabel("Measurement")
                val yAxis = new NumberAxis
                yAxis.setForceZeroInRange(false)

                // Define the chart
                val lineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)
                lineChart.setTitle(sensorMeasurements._1 + "; Range: 0ms -> " + result.maxRangeMs + "ms; " + result.description) // Sensor name
                lineChart.setCreateSymbols(false)

                // Define series
                val series = new XYChart.Series[Number, Number]
                series.setName(sensorMeasurements._1)

                analysisService.getFirstWindow(sensorMeasurements._2, result.maxRangeMs).foreach(measurement => {
                    // Populate the series with data
                    series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement), measurement.value))
                })
                lineChart.getData.add(series)

                // Add horizontal value markers
                result.horizontalMarkers.foreach(marker => {
                    lineChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, marker))
                })

                // Add vertical value markers
                result.verticalMarkers.foreach(range => {
                    if (range._1 == range._2)
                        lineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
                    else
                        lineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
                })

                // Set line style
                series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")

                // Return linechart
                lineChart
            })

            // return sensor results
            (results, sensorMeasurements._1 -> linecharts)
        }).toVector

        // Add charts to container
        chartAnalysisView.addCharts(sensorResultsMap.map(entry => entry._2._1 -> entry._2._2).toMap, "Sensor range charts", chartAnalysisView.titledPaneContainerSlidingWindow)

        markFullGraph(sensorResultsMap.flatMap(entry => entry._1))


    }

    private def analyseSensorGraphsNormalDist(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Sensor charts
        val sensorChartsMap: Map[String, Vector[LineChartWithMarkers[Number, Number]]] = chartAnalysisView.getChartsFromContainer(chartAnalysisView.titledPaneContainerNormal)

        // Modify sensorGraphs
        val sensorResults = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val baseLine = analysisService.calcBaseLine(sensorMeasurements._2.map(m => m.value))
            val sensorResult = analysisService.analyseNormalDist(sensorMeasurements, 10, sizeWindowOne, 0.95, 100, sizeWindowTwo, 0.97)

            // Define the chart
            val sensorChart = sensorChartsMap(sensorMeasurements._1).head
            // Clear chart of previous analyses
            sensorChart.removeAllMarkers()

            // Add value marker for baseline
            sensorChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, baseLine))

            // Add value markers for sensorResults
            sensorResult.verticalMarkers.foreach(range => {
                if (range._1 == range._2)
                    sensorChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1, 0))
                else
                    sensorChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1, range._2))
            })

            // return sensor result
            sensorResult
        })

        markFullGraph(sensorResults)
    }

    private def markFullGraph(sensorResults: Iterable[SensorResult]): Unit = {
        // Clear chart of previous analyses
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
                analyseSensorGraphsNormalDist
        }
    }

    private def addEventHandlers(): Unit = {
        chartAnalysisView.cmbAnalysisChoice.getSelectionModel.selectedItemProperty().addListener((options, oldValue, newValue) => {
            AnalysisType.withName(chartAnalysisView.cmbAnalysisChoice.getSelectionModel.getSelectedItem) match {
                case AnalysisType.HORIZONTAL_SLIDING_WINDOW | AnalysisType.VERTICAL_SLIDING_WINDOW =>
                    chartAnalysisView.tpContainer.getChildren.set(0, chartAnalysisView.titledPaneContainerSlidingWindow)
                case AnalysisType.NORMAL_DISTRIBUTION | AnalysisType.DIFFERENTIAL_NORMAL_DISTRIBUTION =>
                    chartAnalysisView.tpContainer.getChildren.set(0, chartAnalysisView.titledPaneContainerNormal)
            }
        })

        chartAnalysisView.btnAnalyse.setOnMouseClicked(_ => {
            chooseAnalysisStrategy().apply(4, 1)
        })

        chartAnalysisView.btnClearAllMarkers.setOnMouseClicked(_ => {
            // Full chart
            chartAnalysisView.fullLineChart.removeAllMarkers()

            // Graphs from containers
            Vector(chartAnalysisView.titledPaneContainerSlidingWindow, chartAnalysisView.titledPaneContainerNormal)
              .flatMap(chartAnalysisView.getChartsFromContainer)
              .foreach(entry => entry._2.foreach(lc => lc.removeAllMarkers()))
        })

        chartAnalysisView.btnClearHMarkers.setOnMouseClicked(_ => {
            // Full chart
            chartAnalysisView.fullLineChart.removeAllHorizontalValueMarkers()

            // Graphs from containers
            Vector(chartAnalysisView.titledPaneContainerSlidingWindow, chartAnalysisView.titledPaneContainerNormal)
              .flatMap(chartAnalysisView.getChartsFromContainer)
              .foreach(entry => entry._2.foreach(lc => lc.removeAllHorizontalValueMarkers()))
        })

        chartAnalysisView.btnClearVMarkers.setOnMouseClicked(_ => {
            // Full chart
            chartAnalysisView.fullLineChart.removeAllVerticalValueMarkers()
            chartAnalysisView.fullLineChart.removeAllVerticalRangeMarkers()

            // Graphs from containers
            Vector(chartAnalysisView.titledPaneContainerSlidingWindow, chartAnalysisView.titledPaneContainerNormal)
              .flatMap(chartAnalysisView.getChartsFromContainer)
              .foreach(entry => {
                  entry._2.foreach(lc => {
                      lc.removeAllVerticalValueMarkers()
                      lc.removeAllVerticalRangeMarkers()
                  })
              })
        })

        chartAnalysisView.chcmbSensors.getCheckModel.getCheckedItems.addListener(new ListChangeListener[String] {
            override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
                while (c.next()) {
                    if (c.wasAdded()) {
                        val tpSliding = chartAnalysisView.titledPaneContainerSlidingWindow.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.replace("Sensor: ", "").equalsIgnoreCase(c.getAddedSubList.get(0)))
                          .get(0)
                        tpSliding.setVisible(true)
                        tpSliding.setManaged(true)

                        val tpNormal = chartAnalysisView.titledPaneContainerNormal.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.replace("Sensor: ", "").equalsIgnoreCase(c.getAddedSubList.get(0)))
                          .get(0)
                        tpNormal.setVisible(true)
                        tpNormal.setManaged(true)

                        val seriesNode = chartAnalysisView.fullLineChart.getData
                          .filtered(serie => serie.getName.equalsIgnoreCase(c.getAddedSubList.get(0)))
                          .get(0).getNode
                        seriesNode.setVisible(true)
                        seriesNode.setManaged(true)
                    } else if (c.wasRemoved()) {
                        val tpSliding = chartAnalysisView.titledPaneContainerSlidingWindow.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.replace("Sensor: ", "").equalsIgnoreCase(c.getRemoved.get(0)))
                          .get(0)
                        tpSliding.setVisible(false)
                        tpSliding.setManaged(false)

                        val tpNormal = chartAnalysisView.titledPaneContainerNormal.getChildren
                          .filtered(n => n.isInstanceOf[TitledPane])
                          .filtered(n => n.asInstanceOf[TitledPane].getText.replace("Sensor: ", "").equalsIgnoreCase(c.getRemoved.get(0)))
                          .get(0)
                        tpNormal.setVisible(false)
                        tpNormal.setManaged(false)

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
