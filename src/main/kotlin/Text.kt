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
    private val textHeight = bounds.height - (padding * 2)
    private val lineHeight = minOf(textHeight.toInt(), 24)

    override fun draw(context: CanvasRenderingContext2D) {
        context.save()
        context.fillStyle = "rgb(255, 0, 0)"
        context.strokeStyle = "rgb(255, 255, 255)"
        context.setLineDash(emptyArray())
        context.textBaseline = CanvasTextBaseline.HANGING
        context.font = "${lineHeight}px bold serif"
        context.lineWidth = 2.0

        if (text.isNotBlank()) {
            drawText(text, context)
        } else {
            context.fillStyle = "rgba(100, 100, 100, .7)"
            context.strokeStyle = "rgba(255, 255, 255, .7)"
            drawText("Type to add comment", context)
        }

        context.restore()
    }

    private fun drawText(text: String, context: CanvasRenderingContext2D) =
            text.split("\n|\r\n".toRegex())
                    .flatMap { line ->
                        when {
                            context.measureText(line).width <= textWidth -> listOf(line)
                            else -> wrapLine(line, context)
                        }
                    }.forEachIndexed { i, wrappedLine ->
                        context.strokeText(wrappedLine, bounds.x + padding, bounds.y + padding + (i * lineHeight))
                        context.fillText(wrappedLine, bounds.x + padding, bounds.y + padding + (i * lineHeight))
                    }

    private fun wrapLine(line: String, context: CanvasRenderingContext2D): List<String> {
        val words = line.split(" ")
        var index = words.size

        while (index > 0 && context.measureText(words.subList(0, index).joinToString("", " ")).width > textWidth)
            index--

        if (index == 0)
            return emptyList()

        val keep = words.subList(0, index).joinToString(" ")
        val rest = words.subList(index, words.size).joinToString(" ")

        val wrappedLines = mutableListOf(keep)
        if (rest.isNotBlank())
            wrappedLines.addAll(wrapLine(rest, context))
        return wrappedLines
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
        if (editing) {
            savePending(input.value)
        }
        startPoint = point
        endPoint = point
        mouseDown = true
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

    private fun textFinalized(event: Event) = savePending(event.target?.asDynamic().value)

    private fun savePending(value: String) {
        if (value.isNotBlank())
            model.orderedCanvasItems.add(Text(value, startPoint, endPoint))
        reset()
        renderService.draw()
    }

    private fun inputKeyPress(event: Event) {
        if (event is KeyboardEvent && event.keyCode == 13) //enter key
            input.blur()
    }
}