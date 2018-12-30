package stimuli.utils

import javafx.scene.control.TextField
import javafx.scene.{Node, Parent}

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         08/12/2018 19:49
  *
  */
class NodeFinder {
    def getNodeByUserData(parent: Parent, userData: Any): Node = {
        getNodesByUserData(parent, userData)(0)
    }

    def getNodesByUserData(parent: Parent, userData: Any): Vector[Node] = {
        parent.getChildrenUnmodifiable.toArray.toStream.filter(o => o.isInstanceOf[TextField]).map(o => o.asInstanceOf[TextField]).filter(tf => tf.getUserData.equals(userData)).toVector
    }
}
