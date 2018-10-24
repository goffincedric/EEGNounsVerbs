package stimulus.view.demo

import javafx.geometry.Pos
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control.{Label, ScrollPane, Tab, TabPane}
import javafx.scene.layout.VBox
import stimulus.model.Stimulus

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class DemoView extends TabPane {

    def addDataPane(data: (Vector[Stimulus], Vector[Stimulus]), name: String): Unit = {
        // Create root element
        val graphContainer = new VBox()
        graphContainer.setAlignment(Pos.CENTER)
        graphContainer.setSpacing(30.0)
        graphContainer.getChildren.add(new Label("EEG data " + name))


        // Create the graphs
        for (stimulus <- data._1 ++ data._2) {
            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis

            // Define the chart
            val lineChart = new LineChart[Number, Number](xAxis, yAxis)
            lineChart.setTitle("Word: " + stimulus.word + " (Type: " + stimulus.stimulusType + ")")

            var counter: Int = 0
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

            // Add chart to root
            graphContainer.getChildren.add(lineChart)
        }

        // Create scrollpane for scrollable graphs
        val root = new ScrollPane(graphContainer)
        root.setFitToWidth(true)

        // Create tab
        val tab = new Tab(name)
        tab.setContent(root)
        tab.closableProperty().setValue(false)

        // Add tab to tabPane
        this.getTabs.add(tab)
    }
}
