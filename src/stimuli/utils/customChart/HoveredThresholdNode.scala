package stimuli.utils.customChart

import javafx.scene.layout.StackPane
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent

/**
  * @author Cédric Goffin
  *         14/12/2018 10:18
  *
  */
class HoveredThresholdNode[X, Y](priorValue: X, value: Y) extends StackPane {
    this.setPrefSize(15, 15)

    val label: Label = createDataThresholdLabel()

    setOnMouseEntered((mouseEvent: MouseEvent) => {
        getChildren.setAll(label)
        setCursor(Cursor.NONE)
        toFront()
    })
    setOnMouseExited((mouseEvent: MouseEvent) => {
        getChildren.clear()
        setCursor(Cursor.CROSSHAIR)
    })

    private def createDataThresholdLabel(): Label = {
        val label = new Label(value + "")
        label.getStyleClass.addAll("default-color0", "chart-line-symbol", "chart-series-line")
        label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;")
        if (priorValue == 0) label.setTextFill(Color.DARKGRAY)
        else if (value.asInstanceOf[Number].doubleValue > priorValue.asInstanceOf[Number].doubleValue) label.setTextFill(Color.FORESTGREEN)
        else label.setTextFill(Color.FIREBRICK)
        label.setMinSize(50, 50)
        label
    }
}
