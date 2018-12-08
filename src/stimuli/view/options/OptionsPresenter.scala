package stimuli.view.options

import javafx.scene.control.{Button, TextField}
import javafx.stage.Stage
import stimuli.model.Stimuli
import stimuli.model.option.AnalysisOption
import stimuli.services.options.OptionsService
import stimuli.utils.NodeFinder

/**
  * @author Cédric Goffin
  *         07/12/2018 09:52
  *
  */
class OptionsPresenter(private val model: Stimuli, private val optionsView: OptionsView) {
    private val optionsService = new OptionsService(model.optionsFilePath)

    init()
    addEventHandlers()

    private def init(): Unit = {
        val nodeFinder = new NodeFinder
        optionsService.getOptions.foreach(option => {
            (nodeFinder.getNodesByUserData(optionsView.slidingWindowOptionsPane, option.name) ++ nodeFinder.getNodesByUserData(optionsView.normalDistOptionsPane, option.name))
              .head.asInstanceOf[TextField].setText(option.value)
        })
    }

    private def addEventHandlers(): Unit = {
        optionsView.btnSave.setOnMouseClicked(event => {
            val options = Array(
                // Sliding Window Pane
                new AnalysisOption("SlidingRangeSizeIncrement", optionsView.txtSlidingRangeSizeIncrement.getText),
                new AnalysisOption("SlidingMaxRangeSize", optionsView.txtSlidingMaxRangeSize.getText),
                new AnalysisOption("SlidingSplitPointMs", optionsView.txtSlidingSplitPointMs.getText),
                new AnalysisOption("SlidingWindowOneMs", optionsView.txtSlidingWindowOneMs.getText),
                new AnalysisOption("SlidingSizeWindowOne", optionsView.txtSlidingSizeWindowOne.getText),
                new AnalysisOption("SlidingWindowTwoMs", optionsView.txtSlidingWindowTwoMs.getText),
                new AnalysisOption("SlidingSizeWindowTwo", optionsView.txtSlidingSizeWindowTwo.getText),
                // Normal Distribution Pane
                new AnalysisOption("NormalDistSplitPointMs", optionsView.txtNormalDistSplitPointMs.getText),
                new AnalysisOption("NormalDistWindowOneMs", optionsView.txtNormalDistWindowOneMs.getText),
                new AnalysisOption("NormalDistSizeWindowOne", optionsView.txtNormalDistSizeWindowOne.getText),
                new AnalysisOption("NormalDistProbTriggerWindowOne", optionsView.txtNormalDistProbTriggerWindowOne.getText),
                new AnalysisOption("NormalDistWindowTwoMs", optionsView.txtNormalDistWindowTwoMs.getText),
                new AnalysisOption("NormalDistSizeWindowTwo", optionsView.txtNormalDistSizeWindowTwo.getText),
                new AnalysisOption("NormalDistProbTriggerWindowTwo", optionsView.txtNormalDistProbTriggerWindowTwo.getText)
            )
            // Write options to file
            optionsService.writeOptions(options)
            // Close Window
            event.getSource.asInstanceOf[Button].getScene.getWindow.asInstanceOf[Stage].close()
        })
    }
}
