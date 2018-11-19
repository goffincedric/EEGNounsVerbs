package stimuli.view.demo

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.LineChart
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, HBox, VBox}

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class DemoView extends TabPane {
    def addTab(name: String, chartsMap: Map[String, LineChart[Number, Number]]): Unit = {
        // Create scrollpane for scrollable graphs
        val title = new Label("EEG data " + name)
        title.setPadding(new Insets(10))
        title.setMaxWidth(Double.MaxValue)
        title.setAlignment(Pos.CENTER)
        title.setScaleX(1.5)
        title.setScaleY(1.5)

        // Container for graphs
        val vbox = new VBox(title)

        // Add graphs to container
        chartsMap.map(entry => {
            // TitledPane with data
            val buttonsContainer = new HBox(new Button("Analyse chart"))
            buttonsContainer.setUserData("graphActions")
            val tp = new TitledPane(
                entry._1, // Title
                new VBox(
                    entry._2, // Chart
                    buttonsContainer // HBox with buttons for chart actions
                )
            )
            tp.setExpanded(false)
            tp
        }).foreach(vbox.getChildren.add)

        // Create scrollpane for scrollable graphs
        val root = new ScrollPane(vbox)
        root.setFitToWidth(true)

        // Create tab
        val tab = new Tab(name)
        tab.setContent(root)
        tab.closableProperty().setValue(false)

        // Add tab to tabPane
        this.getTabs.add(tab)
    }
}
