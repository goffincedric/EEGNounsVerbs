package stimuli.model.stimulus

/**
  * @author Cédric Goffin
  *         12/11/2018 14:24
  *
  */
class Measurement(private val _value: Double, private val _delay: Double) {
    // Getters
    def value: Double = _value
    def delay: Double = _delay
}
