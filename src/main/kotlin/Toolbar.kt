import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLImageElement
import kotlin.coroutines.experimental.buildIterator
import kotlin.coroutines.experimental.buildSequence

internal class Toolbar(private val backgroundItem: BackgroundItem, private val renderService: RenderService, private val model: AnnotationModel) : CanvasItem {

    private val buttons = mutableListOf<Button>()
    val orderedCanvasItems get() = listOf<CanvasItem>(this).plus(buttons)
    val hide = false

    suspend fun init() {

        val rectangleButton = Button(renderService::draw, buttonLocations.next(), createImage("highlightMode.svg"))
        val textButton = Button(renderService::draw, buttonLocations.next(), createImage("textMode.svg"))
        val undoButton = Button(renderService::draw, buttonLocations.next(), createImage("undo.svg"))

        rectangleButton.clickAction = {
            console.log("Rectangle mode enabled")
            resetAll()
            rectangleButton.toggled = true
            backgroundItem.changeMode(Mode.RECTANGLE)
        }
        textButton.clickAction = {
            console.log("Text mode enabled")
            resetAll()
            textButton.toggled = true
            backgroundItem.changeMode(Mode.TEXT)
        }
        undoButton.clickAction = {
            backgroundItem.reset()
            model.undoLastChange()
        }

        with(buttons) {
            add(rectangleButton)
            add(textButton)
            add(undoButton)
        }

        // default to rectangle mode
        rectangleButton.clickAction?.invoke()
    }

    private val buttonLocations = buildIterator {
        var x = PADDING
        val y = PADDING

        while(true) {
            yield(Point(x,y))
            x += BUTTON_WIDTH + (PADDING*2)
        }
    }

    override val bounds: Rectangle
        get() {
            val totalWidth = buttons.map { it.bounds.width + (PADDING*2) }.reduce(Double::plus)
            return Rectangle(Point(0, 0), Point(totalWidth, BUTTON_HEIGHT + (PADDING*2)))
        }

    override fun draw(context: CanvasRenderingContext2D) {
        context.fillStyle = "rgb(236, 230, 216)"
        context.fillRect(bounds)
    }

    fun resetAll() = buttons.forEach { it.toggled = false }

}

private const val PADDING = 4.0
private const val BUTTON_WIDTH = 25.0
private const val BUTTON_HEIGHT = 25.0

internal class Button(reDraw: () -> Unit, location: Point, val image: HTMLImageElement) : MouseDownHandler, MouseUpHandler, MouseEnterHandler, MouseLeaveHandler, CanvasItem {

    override val bounds = Rectangle(location, Point(location.x + BUTTON_WIDTH, location.y + BUTTON_HEIGHT))
    var mouseHover = false
    var mouseDownOnButton = false
    var toggled = false
    var clickAction: (() -> Unit)? = null

    override fun draw(context: CanvasRenderingContext2D) {
        when {
            mouseDownOnButton -> drawBackground("rgb(200, 189, 165)", context)
            mouseHover && !toggled -> drawBackground("rgb(255, 253, 248)", context)
            toggled -> drawBackground("rgb(200, 189, 165)", context)
        }

        context.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height)
    }

    private fun drawBackground(style: String, context: CanvasRenderingContext2D) {
        context.fillStyle = style
        context.fillRect(bounds)
    }

    override val onMouseDown = mouseEventHandler {
        mouseDownOnButton = true
        preventPropagation()
        reDraw()
    }

    override val onMouseUp = mouseEventHandler {
        if (mouseDownOnButton) {
            clickAction?.invoke()
            preventPropagation()
        }
        mouseDownOnButton = false
        reDraw()
    }

    override val onMouseEnter = mouseEventHandler {
        mouseHover = true
        reDraw()
    }

    override val onMouseLeave = mouseEventHandler {
        mouseDownOnButton = false
        mouseHover = false
        reDraw()
    }
}
