package stimuli.view.chartAnalysis

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.LineChart
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class ChartAnalysisView(title: String) extends ScrollPane {
    // Create title label
    val lblTitle = new Label(title)
    lblTitle.setPadding(new Insets(10))
    lblTitle.setMaxWidth(Double.MaxValue)
    lblTitle.setAlignment(Pos.CENTER)
    lblTitle.setScaleX(1.5)
    lblTitle.setScaleY(1.5)

    // Container for graphs
    val titledPaneContainer = new VBox(lblTitle)

    // Add vbox to scrollPane
    this.setContent(titledPaneContainer)
    this.setFitToWidth(true)

    def addCharts(chartsMap: Vector[LineChart[Number, Number]]) : Unit = {
        // Add graphs to container
        chartsMap.foreach(chart => {
            // TitledPane with data
            val tp = new TitledPane(
                "Sensor: " + chart.getTitle, // Title
                new VBox(
                    chart, // Chart
                    new Label("") // Text for analysis output
                )
            )
            tp.setExpanded(true)
            titledPaneContainer.getChildren.add(tp)
        })
    }
}
