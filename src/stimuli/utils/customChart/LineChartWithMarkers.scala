package stimuli.utils.customChart

import java.util.Objects

import javafx.collections.FXCollections
import javafx.scene.chart.{Axis, LineChart, XYChart}
import javafx.scene.shape.Line

/**
  * @author Cédric Goffin
  *         26/11/2018 13:06
  *
  */
class LineChartWithMarkers[X, Y](val xAxis: Axis[X], val yAxis: Axis[Y]) extends LineChart[X, Y](xAxis, yAxis) {
    private val horizontalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    horizontalMarkers.addListener(_ => layoutPlotChildren())
    private val verticalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    verticalMarkers.addListener(_ => layoutPlotChildren())

    def addHorizontalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (horizontalMarkers.contains(marker)) return
        val line = new Line()
        marker.setNode(line)
        getPlotChildren.add(line)
        horizontalMarkers.add(marker)
    }

    def removeHorizontalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        horizontalMarkers.remove(marker)
    }

    def addVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (verticalMarkers.contains(marker)) return
        val line = new Line()
        marker.setNode(line)
        getPlotChildren.add(line)
        verticalMarkers.add(marker)
    }

    def removeVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        verticalMarkers.remove(marker)
    }

    override protected def layoutPlotChildren(): Unit = {
        super.layoutPlotChildren()
        horizontalMarkers.forEach(marker => {
            val line = marker.getNode.asInstanceOf[Line]
            line.setStartX(0)
            line.setEndX(getBoundsInLocal.getWidth)
            line.setStartY(getYAxis.getDisplayPosition(marker.getYValue) + 0.5) // 0.5 for crispness

            line.setEndY(line.getStartY)
            line.toFront()
        })
        verticalMarkers.forEach(marker => {
            val line = marker.getNode.asInstanceOf[Line]
            line.setStartX(getXAxis.getDisplayPosition(marker.getXValue) + 0.5)
            line.setEndX(line.getStartX)
            line.setStartY(0d)
            line.setEndY(getBoundsInLocal.getHeight)
            line.toFront()
        })
    }
}
