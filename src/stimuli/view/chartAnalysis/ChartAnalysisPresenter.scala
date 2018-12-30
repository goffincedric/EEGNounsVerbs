package stimuli.view.chartAnalysis

import javafx.collections.ListChangeListener
import javafx.scene.Scene
import javafx.scene.chart.{NumberAxis, XYChart}
import javafx.scene.control.{TitledPane, Tooltip}
import javafx.scene.input.MouseEvent
import javafx.stage.{Modality, Stage}
import stimuli.model.Stimuli
import stimuli.model.analysis.{AnalysisType, SensorResult}
import stimuli.services.analysis.AnalysisService
import stimuli.services.options.OptionsService
import stimuli.utils.customChart.LineChartWithMarkers
import stimuli.view.options.{OptionsPresenter, OptionsView}

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         17/10/2018 16:15
  *
  */
class ChartAnalysisPresenter(private val model: Stimuli, private val name: String, private val word: String, private val chartAnalysisView: ChartAnalysisView) {
    private val analysisService = new AnalysisService
    private val optionsService = new OptionsService(model.optionsFilePath)
    private val stimulus = model.stimuliMapUnsorted(name).filter(s => s.word.equals(word)).head

    // Split stimulus sensor data
    createSensorGraphs(optionsService.getOption("SlidingSizeWindowOne").value.toInt, optionsService.getOption("SlidingSizeWindowTwo").value.toInt)

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
                series.getData.add(new XYChart.Data[Number, Number](cp._2.indexOf(measurement) * model.hardcodedDelayMS, measurement.value))
            }

            // Add series to chart
            chartAnalysisView.fullLineChart.addSeriesToData(series)

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

                // Define series
                val series = new XYChart.Series[Number, Number]
                series.setName(sensorMeasurements._1)

                analysisService.getFirstWindow(sensorMeasurements._2, range).foreach(measurement => {
                    // Populate the series with data
                    series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement) * model.hardcodedDelayMS, measurement.value))
                })
                lineChart.addSeriesToData(series)

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

            // Define series
            val series = new XYChart.Series[Number, Number]
            series.setName(sensorMeasurements._1)

            sensorMeasurements._2.foreach(measurement => {
                // Populate the series with data
                series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement) * model.hardcodedDelayMS, measurement.value))
            })
            lineChart.addSeriesToData(series)

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
        // Get options
        val options = optionsService.getOptions
        val rangeSizeIncrement = options.filter(o => o.name.equals("SlidingRangeSizeIncrement")).head
        val maxRangeSize = options.filter(o => o.name.equals("SlidingMaxRangeSize")).head
        val splitPointMs = options.filter(o => o.name.equals("SlidingSplitPointMs")).head
        val windowOneMs = options.filter(o => o.name.equals("SlidingWindowOneMs")).head
        val sizeWindowOne = options.filter(o => o.name.equals("SlidingSizeWindowOne")).head
        val windowTwoMs = options.filter(o => o.name.equals("SlidingWindowTwoMs")).head
        val sizeWindowTwo = options.filter(o => o.name.equals("SlidingSizeWindowTwo")).head

        // Map of sensorResults and Charts
        val sensorResultsMap = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val results = analysisService.analyseHorizontalSlidingWindow(sensorMeasurements, splitPointMs.value.toDouble, windowOneMs.value.toDouble, sizeWindowOne.value.toInt, 0.95, windowTwoMs.value.toDouble, sizeWindowTwo.value.toInt, 0.97, maxRangeSize.value.toDouble, rangeSizeIncrement.value.toDouble)

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

                // Define series
                val series = new XYChart.Series[Number, Number]
                series.setName(sensorMeasurements._1)

                analysisService.getFirstWindow(sensorMeasurements._2, result.maxRangeMs).foreach(measurement => {
                    // Populate the series with data
                    series.getData.add(new XYChart.Data[Number, Number](sensorMeasurements._2.indexOf(measurement) * model.hardcodedDelayMS, measurement.value))
                })
                lineChart.addSeriesToData(series)

                // Add horizontal value markers
                result.horizontalMarkers.foreach(marker => {
                    lineChart.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, marker))
                })

                // Add vertical value markers
                result.verticalMarkers.foreach(range => {
                    if (range._1 == range._2)
                        lineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, 0))
                    else
                        lineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, range._2 * model.hardcodedDelayMS))
                })

                // Set line style
                series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")

                // Return linechart
                lineChart
            })

            // return sensor results
            (results, sensorMeasurements._1) -> linecharts
        })

        // Collect all old sensor charts and map to respective animation
        val lineChartAnimations = chartAnalysisView.getChartsFromContainer(chartAnalysisView.titledPaneContainerSlidingWindow).values.flatten.map(lc => lc.getAnimation(() => ())).toVector
        val fullLineChartAnimation =
            chartAnalysisView.fullLineChart.getAnimation(() => {
                // Add charts to container
                chartAnalysisView.addCharts(sensorResultsMap.map(entry => entry._1._2 -> entry._2), "Sensor range charts", chartAnalysisView.titledPaneContainerSlidingWindow)

                // Mark full graph
                markFullGraph(sensorResultsMap.keys.flatMap(entry => entry._1))
            })

        // Concat animations and play
        (lineChartAnimations :+ fullLineChartAnimation).foreach(animation => animation.play())
    }

    private def analyseSensorGraphsNormalDist(sizeWindowOne: Int, sizeWindowTwo: Int): Unit = {
        // Get options
        val options = optionsService.getOptions
        val splitPointMs = options.filter(o => o.name.equals("NormalDistSplitPointMs")).head
        val windowOneMs = options.filter(o => o.name.equals("NormalDistWindowOneMs")).head
        val sizeWindowOne = options.filter(o => o.name.equals("NormalDistSizeWindowOne")).head
        val probTriggerWindowOne = options.filter(o => o.name.equals("NormalDistProbTriggerWindowOne")).head
        val windowTwoMs = options.filter(o => o.name.equals("NormalDistWindowTwoMs")).head
        val sizeWindowTwo = options.filter(o => o.name.equals("NormalDistSizeWindowTwo")).head
        val probTriggerWindowTwo = options.filter(o => o.name.equals("NormalDistProbTriggerWindowTwo")).head

        // Sensor charts
        val sensorChartsMap: Map[String, Vector[LineChartWithMarkers[Number, Number]]] = chartAnalysisView.getChartsFromContainer(chartAnalysisView.titledPaneContainerNormal)

        // Modify sensorGraphs
        val sensorResultsChartMap = stimulus.measurements.map(sensorMeasurements => {
            // Analyse chart
            val baseLine = analysisService.calcBaseLine(sensorMeasurements._2.map(m => m.value))
            val sensorResult = analysisService.analyseNormalDist(sensorMeasurements, splitPointMs.value.toDouble, windowOneMs.value.toDouble, sizeWindowOne.value.toInt, probTriggerWindowOne.value.toDouble, windowTwoMs.value.toDouble, sizeWindowTwo.value.toInt, probTriggerWindowTwo.value.toDouble)

            // Define the chart
            val sensorChart = sensorChartsMap(sensorMeasurements._1).head
            // Clear chart of previous analyses
            sensorChart.removeAllMarkers()

            // return sensor result
            (sensorResult, baseLine) -> sensorChart
        })

        // Collect all old sensor charts and map to respective animation
        val lineChartAnimations = sensorChartsMap.values.flatten.map(lc => lc.getAnimation(() => ())).toVector
        val fullLineChartAnimation =
            chartAnalysisView.fullLineChart.getAnimation(() => {
                // Mark sensorCharts
                sensorResultsChartMap.foreach(pair => {
                    // Add value marker for baseline
                    pair._2.addHorizontalValueMarker(new XYChart.Data[Number, Number](0, pair._1._2))

                    // Add value markers for sensorResults
                    pair._1._1.verticalMarkers.foreach(range => {
                        if (range._1 == range._2)
                            pair._2.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, 0))
                        else
                            pair._2.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, range._2 * model.hardcodedDelayMS))
                    })
                })

                // Mark full graph
                markFullGraph(sensorResultsChartMap.keys.map(pair => pair._1))
            })

        // Concat animations and play
        (lineChartAnimations :+ fullLineChartAnimation).foreach(animation => animation.play())
    }

    private def markFullGraph(sensorResults: Iterable[SensorResult]): Unit = {
        // Clear chart of previous analyses
        chartAnalysisView.fullLineChart.removeAllMarkers()

        // Merge SensorResults
        val unifiedSensorResult = analysisService.mergeSensorResultsResults(sensorResults)

        // Vertical markers
        unifiedSensorResult.verticalMarkers.foreach(range => {
            if (range._1 == range._2)
                chartAnalysisView.fullLineChart.addVerticalValueMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, 0))
            else
                chartAnalysisView.fullLineChart.addVerticalRangeMarker(new XYChart.Data[Number, Number](range._1 * model.hardcodedDelayMS, range._2 * model.hardcodedDelayMS))
        })
    }

    private def chooseAnalysisStrategy(): (Int, Int) => Unit = {
        AnalysisType.withName(chartAnalysisView.cmbAnalysisChoice.getSelectionModel.getSelectedItem) match {
            case AnalysisType.HORIZONTAL_SLIDING_WINDOW =>
                analyseSensorGraphsHorSlidingWindow
            case AnalysisType.NORMAL_DISTRIBUTION =>
                analyseSensorGraphsNormalDist
            //            case AnalysisType.DIFFERENTIAL_NORMAL_DISTRIBUTION => //TODO
            //                analyseSensorGraphsNormalDist
        }
    }

    private def addEventHandlers(): Unit = {
        chartAnalysisView.optionsMenuItem.getGraphic.setOnMouseClicked((event: MouseEvent) => {
            val optionsView = new OptionsView("Options Analysis")
            new OptionsPresenter(model, optionsView)
            val newStage = new Stage()
            newStage.initModality(Modality.APPLICATION_MODAL)
            val newScene = new Scene(optionsView)
            newScene.getStylesheets.addAll(chartAnalysisView.getScene.getStylesheets)
            newStage.setScene(newScene)
            newStage.toFront()
            newStage.show()
        })

        chartAnalysisView.cmbAnalysisChoice.getSelectionModel.selectedItemProperty().addListener((options, oldValue, newValue) => {
            AnalysisType.withName(chartAnalysisView.cmbAnalysisChoice.getSelectionModel.getSelectedItem) match {
                case AnalysisType.HORIZONTAL_SLIDING_WINDOW =>
                    chartAnalysisView.tpContainer.getChildren.set(0, chartAnalysisView.titledPaneContainerSlidingWindow)
                case AnalysisType.NORMAL_DISTRIBUTION /*| AnalysisType.DIFFERENTIAL_NORMAL_DISTRIBUTION*/ =>
                    chartAnalysisView.tpContainer.getChildren.set(0, chartAnalysisView.titledPaneContainerNormal)
            }
        })

        chartAnalysisView.btnAnalyse.setOnMouseClicked(_ => chooseAnalysisStrategy().apply(4, 1))

        chartAnalysisView.btnClearAllMarkers.setOnMouseClicked(_ => removeAllChartMarkers())

        chartAnalysisView.btnClearHMarkers.setOnMouseClicked(_ => removeHorizontalChartMarkers())

        chartAnalysisView.btnClearVMarkers.setOnMouseClicked(_ => removeVerticalChartMarkers())

        chartAnalysisView.chcmbSensors.getCheckModel.getCheckedItems.addListener(new ListChangeListener[String] {
            override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
                while (c.next()) {
                    if (c.wasAdded()) {
                        val tpSliding = chartAnalysisView.titledPaneContainerSlidingWindow.getChildren
                          .filtered(_.isInstanceOf[TitledPane])
                          .filtered(_.asInstanceOf[TitledPane].getText.replace("Sensor: ", "").equalsIgnoreCase(c.getAddedSubList.get(0)))
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

    private def removeVerticalChartMarkers(): Unit = {
        // Full chart
        chartAnalysisView.fullLineChart.removeAllMarkers()

        // Graphs from containers
        Vector(chartAnalysisView.titledPaneContainerSlidingWindow, chartAnalysisView.titledPaneContainerNormal)
          .flatMap(chartAnalysisView.getChartsFromContainer)
          .foreach(entry => entry._2.foreach(lc => lc.removeAllMarkers()))
    }

    private def removeHorizontalChartMarkers(): Unit = {// Full chart
        chartAnalysisView.fullLineChart.removeAllHorizontalValueMarkers()

        // Graphs from containers
        Vector(chartAnalysisView.titledPaneContainerSlidingWindow, chartAnalysisView.titledPaneContainerNormal)
          .flatMap(chartAnalysisView.getChartsFromContainer)
          .foreach(entry => entry._2.foreach(lc => lc.removeAllHorizontalValueMarkers()))
    }

    private def removeAllChartMarkers(): Unit = {// Full chart
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
    }
}
