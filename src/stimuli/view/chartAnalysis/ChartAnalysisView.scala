package stimuli.view.chartAnalysis

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.{LineChart, NumberAxis}
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, HBox, VBox}
import org.controlsfx.control.CheckComboBox
import stimuli.model.analysis.AnalysisType
import stimuli.utils.customChart.LineChartWithMarkers

/**
  * @author Cédric Goffin
  *         17/10/2018 16:12
  *
  */
class ChartAnalysisView(title: String) extends ScrollPane {
    /* Top */
    // Title label
    val lblTitle = new Label(title)
    // Controls & conainer HBox
    val chcmbSensors = new CheckComboBox[String]()
    val cmbAnalysisChoice = new ComboBox[String]()
    val btnAnalyse = new Button("Analyse charts")
    val btnClearAllMarkers = new Button("Clear all markers")
    val btnClearVMarkers = new Button("Clear vertical markers")
    val btnClearHMarkers = new Button("Clear horizontal markers")
    val leftNodes = new HBox(10, chcmbSensors, cmbAnalysisChoice, btnAnalyse)
    val rightNodes = new HBox(10, btnClearAllMarkers, btnClearHMarkers, btnClearVMarkers)
    val buttonbar = new HBox(leftNodes, rightNodes)

    /* Center */
    // Full Linechart
    val xAxis = new NumberAxis
    val yAxis = new NumberAxis
    val fullLineChart = new LineChartWithMarkers[Number, Number](xAxis, yAxis)

    // Separator
    val separator = new Separator()

    // Container for graphs in TitledPanes
    val lblSensorsTitle = new Label("Sensor detail charts")
    val titledPaneContainer = new VBox(lblSensorsTitle)

    // Root borderpane
    val root = new BorderPane()
    val topVBox = new VBox(lblTitle, buttonbar)
    val centerHbox = new VBox(fullLineChart, separator, titledPaneContainer)

    layoutNodes()

    def layoutNodes(): Unit = {
        // Layout scrollpane
        this.setContent(root)
        this.setFitToWidth(true)
        this.getStyleClass.add("edge-to-edge")

        // Layout Top
        /* Title lable */
        lblTitle.setPadding(new Insets(10))
        lblTitle.setMaxWidth(Double.MaxValue)
        lblTitle.setAlignment(Pos.CENTER)
        lblTitle.setScaleX(1.5)
        lblTitle.setScaleY(1.5)

        /* Button bar */
        leftNodes.setAlignment(Pos.CENTER_LEFT)
        chcmbSensors.setPadding(new Insets(10))
        AnalysisType.values.foreach(t => cmbAnalysisChoice.getItems.add(t.toString))
        cmbAnalysisChoice.getSelectionModel.selectFirst()
        cmbAnalysisChoice.setPadding(new Insets(0, 10, 0, 10))
        btnAnalyse.setPadding(new Insets(10))
        rightNodes.setAlignment(Pos.CENTER)
        btnClearAllMarkers.setPadding(new Insets(10))
        btnClearHMarkers.setPadding(new Insets(10))
        btnClearVMarkers.setPadding(new Insets(10))

        // Layout Center
        /* Linechart */
        fullLineChart.setCreateSymbols(false)
        fullLineChart.setPadding(new Insets(10))
        yAxis.setForceZeroInRange(false)

        /* Separator */
        //        separator.setStyle("-fx-border-style: solid; -fx-border-width: 1px;")

        /* Sensors title lable */
        lblSensorsTitle.setPadding(new Insets(10))
        lblSensorsTitle.setMaxWidth(Double.MaxValue)
        lblSensorsTitle.setAlignment(Pos.CENTER)
        lblSensorsTitle.setScaleX(1.5)
        lblSensorsTitle.setScaleY(1.5)

        // Assign nodes to borderpane
        root.setTop(topVBox)
        root.setCenter(centerHbox)
    }

    def addCharts(chartsMap: Vector[LineChart[Number, Number]]): Unit = {
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

            // Add to checkComboBox
            chcmbSensors.getItems.add(chart.getTitle)
        })
    }
}
