package stimuli.view.demo

import java.util.stream.Collectors

import javafx.collections.transformation.FilteredList
import javafx.scene.{Node, Parent}
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import stimuli.model.Stimuli
import stimuli.model.analysis.AnalysisService
import stimuli.model.stimulus.Stimulus

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class DemoPresenter(private val model: Stimuli, private val demoView: DemoView) {
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
        demoView.addTab(stimuli._1, lineChartsMap)
    }

    private def addEventHandlers() : Unit = {
        demoView.getTabs.forEach(tab => {
            //TODO: set eventhandlers for chart buttons
            val vbox = tab.getContent.asInstanceOf[ScrollPane].getContent.asInstanceOf[VBox]
            val titledPanes = vbox.getChildren.toArray
              .toStream.withFilter(node => node.isInstanceOf[TitledPane])
              .map(node => node.asInstanceOf[TitledPane])
              .toVector

            titledPanes.foreach(tp => {
                val buttons = tp.getContent.asInstanceOf[VBox].getChildren.get(1).asInstanceOf[HBox].getChildren
                buttons.forEach(button => button.setOnMouseClicked(_ => {
                    val alert = new Alert(AlertType.INFORMATION, "Hello", ButtonType.OK)
                    alert.show()
                }))
            })
        })
    }

    private def analyseChartData(word: String, name: String) : Unit = {

        null
    }

    private def getByUserData(parent: Parent, userData: Any): FilteredList[Node] = {
        parent.getChildrenUnmodifiable.filtered(node => node.getUserData.equals(userData))
    }
}
