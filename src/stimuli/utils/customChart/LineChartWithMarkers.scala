package stimuli.utils.customChart

import java.util.Objects

import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.chart.{Axis, LineChart, XYChart}
import javafx.scene.paint.Color
import javafx.scene.shape.{Line, Rectangle}

/**
  * @author Cédric Goffin
  *         26/11/2018 13:06
  *
  */
class LineChartWithMarkers[X, Y](val xAxis: Axis[X], val yAxis: Axis[Y]) extends LineChart[X, Y](xAxis, yAxis) {
    private val _horizontalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    _horizontalMarkers.addListener(_ => layoutPlotChildren())
    private val _verticalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    _verticalMarkers.addListener(_ => layoutPlotChildren())

    private val _verticalRangeMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    _verticalMarkers.addListener(_ => layoutPlotChildren())

    def getHorizontalValueMarkers: ObservableList[XYChart.Data[X, Y]] = this._horizontalMarkers

    def getVerticalValueMarkers: ObservableList[XYChart.Data[X, Y]] = this._verticalMarkers

    def getVerticalRangeMarkers: ObservableList[XYChart.Data[X, Y]] = this._verticalRangeMarkers

    def addHorizontalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (_horizontalMarkers.contains(marker)) return
        val line = new Line()
        marker.setNode(line)
        getPlotChildren.add(line)
        _horizontalMarkers.add(marker)
    }

    def removeHorizontalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _horizontalMarkers.remove(marker)
    }

    def removeAllHorizontalValueMarkers(): Unit = {
        _horizontalMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _horizontalMarkers.clear()
    }

    def addVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (_verticalMarkers.contains(marker)) return
        val line = new Line()
        marker.setNode(line)
        getPlotChildren.add(line)
        _verticalMarkers.add(marker)
    }

    def removeVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _verticalMarkers.remove(marker)
    }

    def removeAllVerticalValueMarkers(): Unit = {
        _verticalMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _verticalMarkers.clear()
    }

    def addVerticalRangeMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (_verticalRangeMarkers.contains(marker)) return
        val rectangle = new Rectangle(0, 0, 0, 0)
        rectangle.setStroke(Color.TRANSPARENT)
        rectangle.setFill(Color.color(Math.random(), Math.random(), Math.random(), 0.2))
        marker.setNode(rectangle)
        getPlotChildren.add(rectangle)
        _verticalRangeMarkers.add(marker)
    }

    def removeVerticalRangeMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _verticalRangeMarkers.remove(marker)
    }

    def removeAllVerticalRangeMarkers(): Unit = {
        _verticalRangeMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _verticalRangeMarkers.clear()
    }

    def removeAllMarkers(): Unit = {
        removeAllHorizontalValueMarkers()
        removeAllVerticalValueMarkers()
        removeAllVerticalRangeMarkers()
    }

    override protected def layoutPlotChildren(): Unit = {
        super.layoutPlotChildren()
        _horizontalMarkers.forEach(marker => {
            val line = marker.getNode.asInstanceOf[Line]
            line.setStartX(0)
            line.setEndX(getBoundsInLocal.getWidth)
            line.setStartY(getYAxis.getDisplayPosition(marker.getYValue) + 0.5) // 0.5 for crispness

            line.setEndY(line.getStartY)
            line.toFront()
        })
        _verticalMarkers.forEach(marker => {
            val line = marker.getNode.asInstanceOf[Line]
            line.setStartX(getXAxis.getDisplayPosition(marker.getXValue) + 0.5) // 0.5 for crispness
            line.setEndX(line.getStartX)
            line.setStartY(0d)
            line.setEndY(getBoundsInLocal.getHeight)
            line.toFront()
        })
        _verticalRangeMarkers.forEach(marker => {
            val rectangle: Rectangle = marker.getNode.asInstanceOf[Rectangle]
            rectangle.setX(getXAxis.getDisplayPosition(marker.getXValue) + 0.5) // 0.5 for crispness

            rectangle.setWidth(getXAxis.getDisplayPosition(marker.getYValue.asInstanceOf[X]) - getXAxis.getDisplayPosition(marker.getXValue))
            rectangle.setY(0d)
            rectangle.setHeight(getBoundsInLocal.getHeight)
        })
    }
}
