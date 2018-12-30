package stimuli.utils.customChart

import java.util.Objects

import javafx.animation.{Interpolator, PathTransition}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.Cursor
import javafx.scene.chart.{Axis, LineChart, NumberAxis, XYChart}
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.util.Duration

/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         26/11/2018 13:06
  *
  */
class LineChartWithMarkers[X, Y](val xAxis: Axis[X], val yAxis: Axis[Y]) extends LineChart[X, Y](xAxis, yAxis) {
    private val _horizontalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    private val _verticalMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()
    private val _verticalRangeMarkers = FXCollections.observableArrayList[XYChart.Data[X, Y]]()

    initDefaultStyle()

    private def initDefaultStyle(): Unit = {
        this.setCreateSymbols(false)
        this.setAnimated(true)
        this.setCursor(Cursor.CROSSHAIR)
    }

    def addSeriesToData(series: XYChart.Series[X, Y]): Unit = {
        this.getData.add(series)
        //
        //        series.getData.forEach(data => {
        //            val t = new Tooltip(data.getYValue.asInstanceOf[Double].toString)
        //            val node1 = data.getNode
        //            Tooltip.install(data.getNode, t)
        //
        //            val node = t.getOwnerNode
        //            //Adding class on hover
        //            this.setOnMouseEntered(_ => data.getNode.getStyleClass.add("onHover"))
        //            //Removing class on exit
        //            data.getNode.setOnMouseExited(_ => data.getNode.getStyleClass.remove("onHover"))
        //        })
    }

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
        layoutPlotChildren()
    }

    def removeHorizontalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _horizontalMarkers.remove(marker)
        layoutPlotChildren()
    }

    def removeAllHorizontalValueMarkers(): Unit = {
        _horizontalMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _horizontalMarkers.removeAll(_horizontalMarkers)
        layoutPlotChildren()
    }

    def addVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (_verticalMarkers.contains(marker)) return
        val line = new Line()
        marker.setNode(line)
        getPlotChildren.add(line)
        _verticalMarkers.add(marker)
        layoutPlotChildren()
    }

    def removeVerticalValueMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _verticalMarkers.remove(marker)
        layoutPlotChildren()
    }

    def removeAllVerticalValueMarkers(): Unit = {
        _verticalMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _verticalMarkers.removeAll(_verticalMarkers)
        layoutPlotChildren()
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
        layoutPlotChildren()
    }

    def removeVerticalRangeMarker(marker: XYChart.Data[X, Y]): Unit = {
        Objects.requireNonNull(marker, "the marker must not be null")
        if (marker.getNode != null) {
            getPlotChildren.remove(marker.getNode)
            marker.setNode(null)
        }
        _verticalRangeMarkers.remove(marker)
        layoutPlotChildren()
    }

    def removeAllVerticalRangeMarkers(): Unit = {
        _verticalRangeMarkers.forEach(marker => {
            if (marker.getNode != null) {
                getPlotChildren.remove(marker.getNode)
                marker.setNode(null)
            }
        })
        _verticalRangeMarkers.removeAll(_verticalRangeMarkers)
        layoutPlotChildren()
    }

    def removeAllMarkers(): Unit = {
        removeAllHorizontalValueMarkers()
        removeAllVerticalValueMarkers()
        removeAllVerticalRangeMarkers()
    }

    def getAnimation(onFinishedCallBack: () => Unit): PathTransition = {
        // Remove all markers for visibility
        removeAllMarkers()

        // Create marker for rectangle
        val marker = new XYChart.Data[X, Y](0.asInstanceOf[X], 50.asInstanceOf[Y])
        addVerticalRangeMarker(marker)
        val markerRect = marker.getNode.asInstanceOf[Rectangle]
        markerRect.setFill(Color.color(0.20784313, 0.58823529, 1, 0.2))

        // Create path for transition
        val path = generatePath()
        // Add path to graph plot
        getPlotChildren.add(path)
        // Generate transition from rectangle and path
        val transition = generatePathTransition(markerRect, path)

        // Cleanup after transition has ended
        transition.setOnFinished(finishTransition(_, path, markerRect, marker, onFinishedCallBack))

        transition
    }

    private def generatePathTransition(shape: Shape, path: Path): PathTransition = {
        val pathTransition = new PathTransition
        pathTransition.setInterpolator(Interpolator.EASE_BOTH)
        pathTransition.setDuration(Duration.seconds(1))
        pathTransition.setPath(path)
        pathTransition.setNode(shape)
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT)
        pathTransition.setCycleCount(2)
        pathTransition.setAutoReverse(true)
        pathTransition
    }

    private def generatePath(): Path = {
        val data = getDisplayedSeriesIterator.next().getData
        val yBounds = yAxis.asInstanceOf[NumberAxis]
        val path = new Path
        path.setOpacity(0)
        path.getElements.add(new MoveTo(xAxis.getDisplayPosition(data.get(0).getXValue), yAxis.asInstanceOf[NumberAxis].getDisplayPosition((yBounds.getUpperBound + yBounds.getLowerBound) / 2)))
        path.getElements.add(new LineTo(xAxis.getDisplayPosition(data.get(data.size() - 1).getXValue), yAxis.asInstanceOf[NumberAxis].getDisplayPosition((yBounds.getUpperBound + yBounds.getLowerBound) / 2)))
        path
    }

    private def finishTransition(event: ActionEvent, path: Path, markerRect: Rectangle, marker: XYChart.Data[X, Y], onFinishedCallBack: () => Unit): Unit = {
        // Remove used nodes and animation components
        getPlotChildren.removeAll(path, markerRect)
        removeVerticalRangeMarker(marker)

        // Execute callBack
        onFinishedCallBack.apply()
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
            val rectangle = marker.getNode.asInstanceOf[Rectangle]
            rectangle.setX(getXAxis.getDisplayPosition(marker.getXValue) + 0.5) // 0.5 for crispness

            rectangle.setWidth(getXAxis.getDisplayPosition(marker.getYValue.asInstanceOf[X]) - getXAxis.getDisplayPosition(marker.getXValue))
            rectangle.setY(0d)
            rectangle.setHeight(getBoundsInLocal.getHeight)
        })
    }
}
