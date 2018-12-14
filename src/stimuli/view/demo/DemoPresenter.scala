package stimuli.view.demo

import javafx.collections.transformation.FilteredList
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.{Node, Parent, Scene}
import javafx.stage.{Screen, Stage}
import stimuli.model.Stimuli
import stimuli.services.analysis.AnalysisService
import stimuli.model.stimulus.Stimulus
import stimuli.utils.customChart.LineChartWithMarkers
import stimuli.view.chartAnalysis.{ChartAnalysisPresenter, ChartAnalysisView}

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class DemoPresenter(private val model: Stimuli, private val demoView: DemoView) {
    //TODO: Scherm maken waar opties ingesteld kunnen worden (standaardafwijking, grootte sliding window (1ste twee seconden = grootte * 10ms, laatste 2 secondsn = grootte * 100ms), ...
    private val analysisService = new AnalysisService

    // Add data to view
    model.stimuliMap.foreach(addDataPane)

    // Add eventhandlers to tabs
    addEventHandlers()

    private def addDataPane(stimuli: (String, (Vector[Stimulus], Vector[Stimulus]))): Unit = {
        // Create the graphs
        val lineChartsMap = (stimuli._2._1 ++ stimuli._2._2).map(stimulus => {
            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis
            yAxis.setForceZeroInRange(false)

            // Define the chart
            val lineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)
            lineChart.setTitle(stimulus.toString)

            for (contact_point <- stimulus.measurements) {
                // Define series
                val series = new XYChart.Series[Number, Number]

                // Populate the series with data
                for (measurement <- contact_point._2) {
                    series.setName(contact_point._1)
                    series.getData.add(new XYChart.Data[Number, Number](contact_point._2.indexOf(measurement) * model.hardcodedDelayMS, measurement.value))
                }

                // Add series to chart
                lineChart.addSeriesToData(series)

                // Set line style
                series.getNode.setStyle("-fx-stroke-width: 2px; -fx-effect: null;")
            }

            // Return tuple with word and corresponding linechart
            (stimulus.word, lineChart)
        }).toMap

        // Add new tab to view
        demoView.addTab(stimuli._1, lineChartsMap)
    }

    private def addEventHandlers(): Unit = {
        demoView.getTabs.forEach(tab => {
            val vbox = tab.getContent.asInstanceOf[ScrollPane].getContent.asInstanceOf[VBox]
            val titledPanes = vbox.getChildren.toArray.toStream
              .withFilter(node => node.isInstanceOf[TitledPane])
              .map(node => node.asInstanceOf[TitledPane])
              .toVector

            titledPanes.foreach(tp => {
                val tpChildren = tp.getContent.asInstanceOf[VBox].getChildren.toArray
                val buttons = tpChildren.toStream
                  .withFilter(node => node.isInstanceOf[HBox])
                  .map(node => node.asInstanceOf[HBox])
                  .filter(hbox => hbox.getId.equals("buttonBox"))
                  .toVector(0).getChildren

                buttons.forEach(button => button.setOnMouseClicked(_ => {
                    val analysisText = tpChildren.toStream
                      .withFilter(node => node.isInstanceOf[Label])
                      .map(node => node.asInstanceOf[Label])
                      .toVector(0)

                    val buttonData = button.getUserData.asInstanceOf[(String, String)]
                    val title = String.format("Graph analysis word: %s, person: %s", buttonData._1, buttonData._2)
                    val chartAnalysisView = new ChartAnalysisView(title)
                    new ChartAnalysisPresenter(model, buttonData._1, buttonData._2, chartAnalysisView)
                    val newStage = new Stage()
                    val newScene = new Scene(chartAnalysisView)
                    newStage.setTitle(title)
                    newScene.getStylesheets.addAll(demoView.getScene.getStylesheets)
                    newStage.setScene(newScene)
                    newStage.setWidth(Screen.getPrimary.getVisualBounds.getWidth)
                    newStage.setHeight(Screen.getPrimary.getVisualBounds.getHeight)
                    newStage.setMaximized(true)
                    newStage.toFront()
                    newStage.show()
                }))
            })
        })
    }
}
