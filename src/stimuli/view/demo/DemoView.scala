package stimuli.view.demo

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.LineChart
import javafx.scene.control._
import javafx.scene.layout.VBox

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class DemoView extends TabPane {
    def addTab(name: String, chartsMap: Map[String, LineChart[Number, Number]]): Tab = {
        // Container for graphs
        val graphContainer = new Accordion()

        // Add graphs to container
        chartsMap.foreach(entry => graphContainer.getPanes.add(new TitledPane(entry._1, entry._2)))

        // Create scrollpane for scrollable graphs
        val title = new Label("EEG data " + name)
        title.setPadding(new Insets(10))
        title.setMaxWidth(Double.MaxValue)
        title.setAlignment(Pos.CENTER)
        title.setScaleX(1.5)
        title.setScaleY(1.5)

        // Create root
        val root = new ScrollPane(new VBox(title, graphContainer))
        root.setFitToWidth(true)

        // Create tab
        val tab = new Tab(name)
        tab.setContent(root)
        tab.closableProperty().setValue(false)

        // Add tab to tabPane
        this.getTabs.add(tab)

        // Return tab
        tab
    }
}
