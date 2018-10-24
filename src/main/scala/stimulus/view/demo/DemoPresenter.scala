package stimulus.view.demo

import stimulus.model.Stimuli

/**
  * @author Cédric Goffin
  *         17/10/2018 16:15
  *
  */
class DemoPresenter(private val model: Stimuli, private val demoView: DemoView) {
    demoView.addDataPane(model.stimuliBart, "Bart")
    demoView.addDataPane(model.stimuliBarbara, "Barbara")
}
