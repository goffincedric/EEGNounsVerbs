package stimulus.view.demo

import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.control.{Label, ScrollPane, Tab, TabPane}
import javafx.scene.layout.VBox

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class DemoView extends TabPane {
    def addTab(name: String, charts: Vector[LineChart[Number, Number]]): Unit = {
        // Container for graphs
        val graphContainer = new VBox()
        graphContainer.setAlignment(Pos.CENTER)
        graphContainer.setSpacing(30.0)
        graphContainer.getChildren.add(new Label("EEG data " + name))

        // Add graphs to container
        charts.foreach(graph => graphContainer.getChildren.add(graph))

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
