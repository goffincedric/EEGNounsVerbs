package stimuli.view.options

import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Label, ScrollPane, TextField}
import javafx.scene.layout.{BorderPane, GridPane, VBox}

/**
  * @author Cédric Goffin
  *         07/12/2018 09:20
  *
  */
class OptionsView(private val title: String) extends ScrollPane {
    val root = new BorderPane

    /* Top */
    // Title label
    val lblTitle = new Label(title)

    /* Center */
    // Sliding window analysis options
    val lblSlidingWindowAnalysisTitle = new Label("1) Sliding window analysis options")
    val slidingWindowOptionsPane = new GridPane
    // Range options
    val lblRangeOptions = new Label("Range options:")
    val lblRangeSizeIncrement = new Label("Range size increment step in millis:")
    val txtRangeSizeIncrement = new TextField()
    val lblMaxRangeSize = new Label("Max range size in millis:")
    val txtMaxRangeSize = new TextField()
    // Sliding window options
    val lblSlidingWindowOptions = new Label("Sliding window options:")
    val lblSlidingSplitPointMs = new Label("Splitting point in millis:")
    val txtSlidingSplitPointMs = new TextField()
    val lblSlidingWindowOneMs = new Label("Size sliding window before split in millis:")
    val txtSlidingWindowOneMs = new TextField()
    val lblSlidingSizeWindowOne = new Label("Sliding window size:")
    val txtSlidingSizeWindowOne = new TextField()
    val lblSlidingWindowTwoMs = new Label("Size sliding window after split in millis:")
    val txtSlidingWindowTwoMs = new TextField()
    val lblSlidingSizeWindowTwo = new Label("Sliding window size:")
    val txtSlidingSizeWindowTwo = new TextField()

    // Separator
    //    val separator = new Separator

    // Normal distribution analysis options
    val lblNormalDistAnalysisTitle = new Label("2) Normal distribution analysis options")
    val normalDistOptionsPane = new GridPane
    // Sliding window options
    val lblNormalDistWindowOptions = new Label("Sliding window options:")
    val lblNormalDistSplitPointMs = new Label("Splitting point in millis:")
    val txtNormalDistSplitPointMs = new TextField()
    val lblNormalDistWindowOneMs = new Label("Size sliding window before split in millis:")
    val txtNormalDistWindowOneMs = new TextField()
    val lblNormalDistSizeWindowOne = new Label("Sliding window size:")
    val txtNormalDistSizeWindowOne = new TextField()
    val lblNormalDistProbTriggerWindowOne = new Label("Probability trigger sliding window one:")
    val txtNormalDistProbTriggerWindowOne = new TextField()
    val lblNormalDistWindowTwoMs = new Label("Size sliding window after split in millis:")
    val txtNormalDistWindowTwoMs = new TextField()
    val lblNormalDistSizeWindowTwo = new Label("Sliding window size:")
    val txtNormalDistSizeWindowTwo = new TextField()
    val lblNormalDistProbTriggerWindowTwo = new Label("Probability trigger sliding window one:")
    val txtNormalDistProbTriggerWindowTwo = new TextField()

    this.layoutNodes()

    private def layoutNodes(): Unit = {
        // Sliding Window Pane
        addTitleToGridPane(slidingWindowOptionsPane, lblSlidingWindowAnalysisTitle, 20, border = true)
        addTitleToGridPane(slidingWindowOptionsPane, lblRangeOptions, 16)
        addOptionToGridPane(slidingWindowOptionsPane, lblRangeSizeIncrement, txtRangeSizeIncrement)
        addOptionToGridPane(slidingWindowOptionsPane, lblMaxRangeSize, txtMaxRangeSize)
        addTitleToGridPane(slidingWindowOptionsPane, lblSlidingWindowOptions, 16)
        addOptionToGridPane(slidingWindowOptionsPane, lblSlidingSplitPointMs, txtSlidingSplitPointMs)
        addOptionToGridPane(slidingWindowOptionsPane, lblSlidingWindowOneMs, txtSlidingWindowOneMs)
        addOptionToGridPane(slidingWindowOptionsPane, lblSlidingSizeWindowOne, txtSlidingSizeWindowOne)
        addOptionToGridPane(slidingWindowOptionsPane, lblSlidingWindowTwoMs, txtSlidingWindowTwoMs)
        addOptionToGridPane(slidingWindowOptionsPane, lblSlidingSizeWindowTwo, txtSlidingSizeWindowTwo)

        // Normal Distribution Pane
        addTitleToGridPane(normalDistOptionsPane, lblNormalDistAnalysisTitle, 20, border = true)
        addTitleToGridPane(normalDistOptionsPane, lblNormalDistWindowOptions, 16)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistSplitPointMs, txtNormalDistSplitPointMs)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistWindowOneMs, txtNormalDistWindowOneMs)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistProbTriggerWindowOne, txtNormalDistProbTriggerWindowOne)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistSizeWindowOne, txtNormalDistSizeWindowOne)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistWindowTwoMs, txtNormalDistWindowTwoMs)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistProbTriggerWindowTwo, txtNormalDistProbTriggerWindowTwo)
        addOptionToGridPane(normalDistOptionsPane, lblNormalDistSizeWindowTwo, txtNormalDistSizeWindowTwo)

        /* Root layout */
        root.setPadding(new Insets(10))

        /* Layout top */
        lblTitle.setPadding(new Insets(10))
        lblTitle.setAlignment(Pos.CENTER)
        lblTitle.setMaxWidth(Double.MaxValue)
        lblTitle.setScaleX(1.5)
        lblTitle.setScaleY(1.5)
        root.setTop(lblTitle)

        /* Layout center */
        root.setCenter(new VBox(10, slidingWindowOptionsPane /*, separator*/ , normalDistOptionsPane))

        /* Scrollpane */
        this.setContent(root)
        this.setFitToWidth(true)
        this.getStyleClass.add("edge-to-edge")
    }

    private def addTitleToGridPane(pane: GridPane, label: Label, fontsize: Int, border: Boolean = false): Unit = {
        label.setPadding(new Insets(10, 0, 0, 0))
        label.setMaxWidth(Double.MaxValue)
        label.setAlignment(Pos.BOTTOM_LEFT)
        label.setStyle(
            (if (border) "-fx-border-color: rgb(200.0, 200.0, 200.0); -fx-border-width: 0 0 2 0;"
            else "") +
              (if (fontsize != 0) "-fx-font-size: " + fontsize + "px;"
              else "")
        )
        pane.add(label, 0, pane.getRowCount, 2, 1)
    }

    private def addOptionToGridPane(pane: GridPane, label: Label, textField: TextField): Unit = {
        val row = pane.getRowCount
        label.setAlignment(Pos.BOTTOM_LEFT)
        textField.setAlignment(Pos.BOTTOM_LEFT)
        pane.add(label, 0, row)
        pane.add(textField, 1, row)
    }
}