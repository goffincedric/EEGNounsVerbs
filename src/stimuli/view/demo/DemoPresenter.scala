package stimuli.view.demo

import javafx.collections.transformation.FilteredList
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.{Node, Parent, Scene}
import javafx.stage.{Screen, Stage}
import stimuli.model.Stimuli
import stimuli.model.analysis.AnalysisService
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
            lineChart.setCreateSymbols(false)

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
        demoView.addTab(stimuli._1, lineChartsMap)
    }

    private def addEventHandlers(): Unit = {
        demoView.getTabs.forEach(tab => {
            //TODO: set eventhandlers for chart buttons
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


                    //                    analysisText.setText("Baseline (mean): " + analysisService.calcBaseLine(model.stimuliMapUnsorted(tab.getText).map(stimulus => stimulus.))) // Gaat niet werken -> analysis text in nieuwe window openen met baseline per sensor per woord
                    //                    val alert = new Alert(AlertType.INFORMATION, "Hello", ButtonType.OK)
                    //                    alert.show()

                    val chartAnalysisView = new ChartAnalysisView(String.format("Graphanalysis word: %s, person: %s", buttonData._1, buttonData._2))
                    new ChartAnalysisPresenter(model, buttonData._1, buttonData._2, chartAnalysisView)
                    val newStage = new Stage()
                    newStage.setScene(new Scene(chartAnalysisView))
                    newStage.setWidth(Screen.getPrimary.getVisualBounds.getWidth)
                    newStage.setHeight(Screen.getPrimary.getVisualBounds.getHeight)
                    newStage.setMaximized(true)
                    newStage.toFront()
                    newStage.show()
                }))

                //                buttons.forEach(button => button.setOnMouseClicked(_ => {
                //
                //                    val alert = new Alert(AlertType.INFORMATION, "Hello", ButtonType.OK)
                //                    alert.show()
                //
                //
                //                }))
            })
        })
    }

    private def analyseChartData(word: String, name: String): Unit = {

        null
    }

    private def getByUserData(parent: Parent, userData: Any): FilteredList[Node] = {
        parent.getChildrenUnmodifiable.filtered(node => node.getUserData.equals(userData))
    }
}
