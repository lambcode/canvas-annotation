import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.abs

internal class Rectangle(point1: Point, point2: Point) {

    val x: Double
    val y: Double
    val width: Double
    val height: Double

    init {
        val x1 = point1.x
        val y1 = point1.y
        val x2 = point2.x
        val y2 = point2.y

        x = when {
            x1 < x2 -> x1
            else -> x2
        }

        y = when {
            y1 < y2 -> y1
            else -> y2
        }

        width = abs(x1 - x2)
        height = abs(y1 - y2)
    }

    fun containsPoint(p: Point) = p.x >= x
            && p.x <= x + width
            && p.y >= y
            && p.y <= y + height
}

internal fun CanvasRenderingContext2D.fillRect(rectangle: Rectangle) = this.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
internal fun CanvasRenderingContext2D.strokeRect(rectangle: Rectangle) = this.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
internal fun CanvasRenderingContext2D.clearRect(rectangle: Rectangle) = this.clearRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)

internal class HighlightRectangle(point1: Point, point2: Point) : CanvasItem {

    override val bounds = Rectangle(point1, point2)

    override fun draw(context: CanvasRenderingContext2D) {
        context.strokeStyle = "rgb(255, 50, 50)"
        context.fillStyle = "rgba(255, 50, 50, .3)"
        context.lineWidth = 2.0
        context.fillRect(bounds)
        context.strokeRect(bounds)
    }
}

internal class RectangleBuilder(private val model: AnnotationModel, private val renderService: RenderService) : Builder, MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseLeaveHandler {
    var startPoint: Point = Point(0.0, 0.0)
    var endPoint: Point = Point(0.0, 0.0)
    var mouseDown = false
    var pendingRectangle: HighlightRectangle? = null

    override fun draw(context: CanvasRenderingContext2D) {
        pendingRectangle?.draw(context)
    }

    override val onMouseDown = mouseEventHandler {
        startPoint = point
        endPoint = point
        mouseDown = true
    }

    override val onMouseMove = mouseEventHandler {
        if (mouseDown) {
            endPoint = point
            pendingRectangle = HighlightRectangle(startPoint, endPoint)
            renderService.draw()
        }
    }

    override val onMouseUp = mouseEventHandler {
        if (mouseDown) {
            if (startPoint != endPoint)
                model.orderedCanvasItems.add(HighlightRectangle(startPoint, endPoint))
            reset()
        }
    }

    override val onMouseLeave = mouseEventHandler { reset() }

    override fun reset() {
        pendingRectangle = null
        mouseDown = false
    }
}
