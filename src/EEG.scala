import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.{Screen, Stage}
import stimuli.model.Stimuli
import stimuli.view.demo.{DemoPresenter, DemoView}

/**
  * @author Cédric Goffin
  *         13/10/2018 12:14
  *
  */
object EEG {
    def main(args: Array[String]): Unit = {
        Application.launch(classOf[EEG], args: _*)
    }
}

class EEG extends Application {
    override def start(primaryStage: Stage): Unit = {
        if (getParameters.getNamed.get("filesPath").isEmpty) {
            System.err.println("No name argument 'filesPath' was given. Please use the argument --filesPath=<Path to data files>")
            System.exit(1)
        } else {
            if (getParameters.getNamed.get("dataDelaysMS").isEmpty) {
                System.err.println("No name argument 'dataDelaysMS' was given. Default delay of 7.8125ms will be used.")
            }
            val model = new Stimuli(getParameters.getNamed.get("filesPath"), "_NounVerb.csv", getParameters.getNamed.get("dataDelaysMS").toDouble)
            val demoview = new DemoView
            val presenter = new DemoPresenter(model, demoview)

            // Set up & show stage
            val scene = new Scene(demoview)
            scene.getStylesheets.add("style/material-fx-v0_3.css")
            scene.getStylesheets.add("style/materialfx-toggleswitch.css")
//            scene.getStylesheets.add("style/Goliath.css")
            primaryStage.setTitle("EEG")
            primaryStage.setScene(scene)
            primaryStage.setWidth(Screen.getPrimary.getVisualBounds.getWidth)
            primaryStage.setHeight(Screen.getPrimary.getVisualBounds.getHeight)
            primaryStage.setMaximized(true)
            primaryStage.toFront()
            primaryStage.show()
        }
    }
}
