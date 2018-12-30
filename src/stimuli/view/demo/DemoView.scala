package stimuli.view.demo

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.LineChart
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         17/10/2018 16:12
  *
  */
class DemoView extends TabPane {
    def addTab(name: String, lineChartsMap: Map[String, LineChart[Number, Number]]): Unit = {
        // Create title label
        val title = new Label("EEG data " + name)
        title.setPadding(new Insets(10))
        title.setMaxWidth(Double.MaxValue)
        title.setAlignment(Pos.CENTER)
        title.setScaleX(1.5)
        title.setScaleY(1.5)

        // Container for graphs
        val titledPaneContainer = new VBox(title)

        // Add graphs to container
        lineChartsMap.foreach(entry => {
            // TitledPane with data
            val analyseChartButton = new Button("Analyse chart")
            analyseChartButton.setUserData((name, entry._1))

            val buttonsContainer = new HBox(analyseChartButton)
            buttonsContainer.setId("buttonBox")

            val tp = new TitledPane(
                entry._1, // Title
                new VBox(
                    entry._2, // Chart
                    new Label(""), // Text for analysis output
                    buttonsContainer // HBox with buttons for chart actions
                )
            )
            tp.setExpanded(false)
            titledPaneContainer.getChildren.add(tp)
        })

        // Create scrollpane for scrollable graphs
        val root = new ScrollPane(titledPaneContainer)
        root.setFitToWidth(true)

        // Create tab
        val tab = new Tab(name)
        tab.setContent(root)
        tab.closableProperty().setValue(false)

        // Add tab to tabPane
        this.getTabs.add(tab)
    }
}
