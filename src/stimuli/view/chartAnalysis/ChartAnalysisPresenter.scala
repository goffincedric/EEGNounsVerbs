package stimuli.view.chartAnalysis

import javafx.scene.chart.{NumberAxis, XYChart}
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
//            val analysisResult = analysisService.analyseData(stimulus, 5, 1 /* , minimum value wnr iets speciaal is? */)

            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis

            // Define the chart
            val lineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)
            lineChart.setTitle(sensorMeasurements._1) // Sensor name

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

            // Return tuple with word and corresponding linechart
            lineChart
        }).toVector

        // Add charts to view
        chartAnalysisView.addCharts(lineChartsMap)
    }

    private def addEventHandlers(): Unit = {
        //TODO:
    }
}
