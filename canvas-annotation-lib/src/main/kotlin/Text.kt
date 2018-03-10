import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document

internal class PendingText(text: String, start: Point, end: Point) : CanvasItem {
    val textBox = Text(text, start, end)
    override val bounds = textBox.bounds

    override fun draw(context: CanvasRenderingContext2D) {
        context.setLineDash(arrayOf(4.0, 2.0))
        context.lineWidth = 1.0
        context.strokeStyle = "rgb(255, 0, 0)"
        context.strokeRect(textBox.bounds)

        textBox.draw(context)
    }
}

internal class Text(private val text: String, start: Point, end: Point) : CanvasItem {
    override val bounds = Rectangle(start, end)
    private val padding = 5
    private val textWidth = bounds.width - (padding * 2)
    private val lineHeight = 24

    override fun draw(context: CanvasRenderingContext2D) {
        context.save()
        context.fillStyle = "rgb(255, 0, 0)"
        context.strokeStyle = "rgb(0, 0, 0)"
        context.setLineDash(emptyArray())
        context.textBaseline = CanvasTextBaseline.HANGING
        context.font = "${lineHeight}px bold serif"
        context.lineWidth = .3

        if (text.isNotBlank()) {
            wrapText(text, context)
        } else {
            context.fillStyle = "rgba(100, 100, 100, .7)"
            context.strokeStyle = "rgba(255, 255, 255, .7)"
            wrapText("Type to add comment", context)
        }

        context.restore()
    }

    private fun wrapText(text: String, context: CanvasRenderingContext2D) {
        val words = text.split(' ')
        var line = ""
        var y = bounds.y + padding

        for(n in 0..words.lastIndex) {
            val testLine = line + words[n] + ' '
            val metrics = context.measureText(testLine)
            val testWidth = metrics.width
            if (testWidth > textWidth && n > 0) {
                context.fillText(line, bounds.x + padding, y)
                context.strokeText(line, bounds.x + padding, y)
                line = words[n] + ' '
                y += lineHeight
            }
            else {
                line = testLine
            }
        }
        context.fillText(line, bounds.x + padding, y)
        context.strokeText(line, bounds.x + padding, y)
    }
}

internal class TextBuilder(private val model: AnnotationModel, private val renderService: RenderService) : Builder, MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseLeaveHandler {
    var startPoint: Point = Point(0.0, 0.0)
    var endPoint: Point = Point(0.0, 0.0)
    var mouseDown = false
    var editing = false
    var input: HTMLInputElement = document.createElement("input") as HTMLInputElement
    var pendingText: PendingText? = null

    init {
        input.type = "text"
        input.style.transform = "scale(0)"
        input.style.position = "absolute"
        input.style.opacity = "0"
        input.style.asDynamic().pointerEvents = "none"
        input.style.asDynamic().userSelect = "none"
        input.oninput = ::textChanged
        input.onblur = ::textFinalized
        input.onkeypress = ::inputKeyPress
        document.body?.appendChild(input) ?: throw IllegalStateException("Could not create input for annotations")
    }

    override fun draw(context: CanvasRenderingContext2D) {
        pendingText?.draw(context)
    }

    override val onMouseDown = mouseEventHandler {
        if (pendingText?.bounds?.containsPoint(point) != true) {
            if (editing) {
                reset()
            }
            startPoint = point
            endPoint = point
            mouseDown = true
        }
    }

    override val onMouseMove = mouseEventHandler {
        if (mouseDown && !editing) {
            endPoint = point
            pendingText = PendingText("", startPoint, endPoint)
            renderService.draw()
        }
    }

    override val onMouseUp = mouseEventHandler {
        if (mouseDown) {
            mouseDown = false
            if (!editing) {
                editing = true
                input.value = ""
                input.focus()
            }
        }
    }

    override val onMouseLeave = mouseEventHandler {
        if (mouseDown)
            reset()
    }

    override fun reset() {
        savePending()
        pendingText = null
        mouseDown = false
        editing = false
        renderService.draw()
    }

    private fun textChanged(event: Event) {
        val value: String = event.target?.asDynamic().value
        pendingText = PendingText(value, startPoint, endPoint)
        renderService.draw()
    }

    private fun textFinalized(event: Event) = savePending()

    private fun savePending() {
        if (input.value.isNotBlank())
            model.orderedCanvasItems.add(Text(input.value, startPoint, endPoint))
        renderService.draw()
    }

    private fun inputKeyPress(event: Event) {
        if (event is KeyboardEvent && event.keyCode == 13) //enter key
            input.blur()
    }
}