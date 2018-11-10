import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import stimulus.model.Stimuli
import stimulus.view.demo.{DemoPresenter, DemoView}

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
        val model = new Stimuli
        val demoview = new DemoView
        val presenter = new DemoPresenter(model, demoview)

        // Set up & show stage
        primaryStage.setTitle("EEG")
        primaryStage.setScene(new Scene(demoview, 1200, 900))
        primaryStage.toFront()
        primaryStage.show()
    }
}
