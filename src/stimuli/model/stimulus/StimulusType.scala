package stimuli.model.stimulus

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         13/10/2018 12:24
  *
  */
object StimulusType extends Enumeration {
    type StimulusType = Value
    val VERB, NOUN, UNKNOWN = Value

    def getType(word: String): StimulusType = {
        word match {
            case "beloof" | "lees" | "geef" | "sta" | "werp" | "promoveer" | "misdraag" | "dien" | "neem" | "ga" | "vang" | "schiet" | "noem" | "rehabiliteer" | "kampeer" => VERB
            case "televisie" | "haag" | "tafel" | "gezicht" | "rietje" | "glas" | "koning" | "meisje" | "vork" | "dakraam" | "penningmeester" | "perkament" | "mes" | "lucifer" | "bes" => NOUN
            case _ => UNKNOWN;
        }
    }
}
