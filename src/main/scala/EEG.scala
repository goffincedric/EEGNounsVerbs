import javafx.application.Application
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.control.{Button, Label, ScrollPane}
import javafx.scene.layout.{AnchorPane, BorderPane, StackPane, VBox}
import javafx.stage.Stage
import stimulus.model.{Stimulus, StimulusType}

import scala.collection.mutable.ListBuffer

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
    private val stimuliBart = readWords("Bart_NounVerb.csv")
    private val stimuliBarbara = readWords("Bart_NounVerb.csv")

    def readWords(path: String): (Vector[Stimulus], Vector[Stimulus]) = {
        val nouns = ListBuffer[Stimulus]()
        val verbs = ListBuffer[Stimulus]()

        // Define contactpoints and their respective column index
        val contact_points = Array(("AF3", 3), ("F7", 4), ("F3", 5), ("FC5", 6), ("T7", 7), ("P7", 8), ("O1", 9), ("O2", 10), ("P8", 11), ("T8", 12), ("FC6", 13), ("F4", 14), ("F8", 15), ("AF4", 16))

        // Initialize vars
        val measurements = scala.collection.mutable.Map[String, ListBuffer[Double]]()
        var word = ""

        // Open reader to file
        val bufferedSource = io.Source.fromResource(path)
        for (line <- bufferedSource.getLines.drop(1)) {
            val cols = line.split("\t").map(_.trim)
            if (cols(0).equalsIgnoreCase("stimulus")) {
                if (!word.equals("")) {
                    val stimulusType = StimulusType.getType(word)
                    val stimulus = new Stimulus(stimulusType, word, measurements map (meting => (meting._1, meting._2.toVector)) toMap)
                    stimulusType match {
                        case StimulusType.NOUN => nouns += stimulus
                        case StimulusType.VERB => verbs += stimulus
                    }
                }
                word = cols(1)
                measurements.clear()
            } else {
                for (contact_point <- contact_points) {
                    measurements.get(contact_point._1) match {
                        case Some(xs: ListBuffer[Double]) =>
                            measurements.update(contact_point._1, xs :+ cols(contact_point._2).toDouble)
                        case None =>
                            measurements += contact_point._1 -> ListBuffer[Double](cols(contact_point._2).toDouble)
                    }
                }

            }
        }

        // Close reader
        bufferedSource.close

        // Return stimuli
        (nouns.toVector, verbs.toVector)
    }

    override def start(primaryStage: Stage): Unit = {
        // Create root element
        val root = new VBox()
        root.setAlignment(Pos.CENTER)
        root.setSpacing(30.0)
        root.getChildren.add(new Label("EEG data Bart"))

        // Create the graphs
        for (stimulus <- stimuliBart._1 ++ stimuliBart._2) {
            // Define the axis
            val xAxis = new NumberAxis
            val yAxis = new NumberAxis

            // Define the chart
            val lineChart = new LineChart[Number, Number](xAxis, yAxis)
            lineChart.setTitle("Word: " + stimulus.word + " (Type: " + stimulus.stimulusType + ")")

            var counter: Int = 0
            for (contact_point <- stimulus.measurements) {
                // Define series
                val series = new XYChart.Series[Number, Number]

                // Populate the series with data
                for (measurement <- contact_point._2) {
                    series.setName(contact_point._1)
                    series.getData.add(new XYChart.Data[Number, Number](counter, measurement))
                    counter += 1
                }
                counter = 0

                // Add series to chart
                lineChart.getData.add(series)
            }

            // Add chart to root
            root.getChildren.add(lineChart)
        }

        // Create scrollpane for scrollable graphs
        val scrollRoot = new ScrollPane(root)
        scrollRoot.setFitToWidth(true)

        // Set up & show stage
        primaryStage.setTitle("EEG")
        primaryStage.setScene(new Scene(scrollRoot, 1200, 900))
        primaryStage.show()
    }
}
