import org.w3c.dom.CanvasRenderingContext2D

internal class Buttons(private val backgroundItem: BackgroundItem, renderService: RenderService) {

    private val rectangleButton = Button(renderService::draw, Point(0, 0)) {
        console.log("Rectangle mode enabled")
        backgroundItem.changeMode(Mode.RECTANGLE)
    }

    private val textButton = Button(renderService::draw, Point(20, 0)) {
        console.log("Text mode enabled")
        backgroundItem.changeMode(Mode.TEXT)
    }

    val orderedCanvasItems get() = listOf(rectangleButton, textButton)
}

private const val BUTTON_WIDTH = 20.0
private const val BUTTON_HEIGHT = 20.0

internal class Button(reDraw: () -> Unit, location: Point, clickAction: () -> Unit) : MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseLeaveHandler, CanvasItem {

    override val bounds = Rectangle(location, Point(location.x + BUTTON_WIDTH, location.y + BUTTON_HEIGHT))
    var mouseHover = false
    var mouseDownOnButton = false

    override fun draw(context: CanvasRenderingContext2D) {
        when {
            mouseDownOnButton -> context.fillStyle = "rgb(0, 0, 255)"
            mouseHover -> context.fillStyle = "rgb(0, 255, 255)"
            else -> context.fillStyle = "rgb(255, 255, 255)"
        }

        context.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    override val onMouseDown = mouseEventHandler {
        if (bounds.containsPoint(point)) {
            mouseDownOnButton = true
            preventPropagation()
        }
        reDraw()
    }

    override val onMouseUp = mouseEventHandler {
        if (mouseDownOnButton && bounds.containsPoint(point)) {
            clickAction()
            preventPropagation()
        }
        mouseDownOnButton = false
        reDraw()
    }

    override val onMouseMove = mouseEventHandler {
        mouseHover = bounds.containsPoint(point)
        reDraw()
    }

    override val onMouseLeave = mouseEventHandler {
        mouseDownOnButton = false
        mouseHover = false
        reDraw()
    }
}
