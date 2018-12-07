package stimuli.view.options

import javafx.scene.control.Label
import javafx.scene.layout.BorderPane

/**
  * @author Cédric Goffin
  *         07/12/2018 09:20
  *
  */
class OptionsView(private val title: String) extends BorderPane {
    /* Top */
    // Title label
    val lblTitle = new Label(title)

    layoutNodes()

    def layoutNodes(): Unit = {

    }

}