import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document

internal class PendingText(private val text: String, start: Point, end: Point) : Drawable {
    val textBox = Text(text, start, end)

    override fun draw(context: CanvasRenderingContext2D) {
        context.setLineDash(arrayOf(4.0, 2.0))
        context.strokeStyle = "rgb(255, 0, 0)"
        context.strokeRect(textBox.rectangle)

        textBox.draw(context)
    }
}

internal class Text(private val text: String, start: Point, end: Point) : Drawable {
    val rectangle = Rectangle(start, end)
    private val padding = 5
    private val textWidth = rectangle.width - (padding * 2)
    private val textHeight = rectangle.height - (padding * 2)
    private val lineHeight = minOf(textHeight.toInt(), 24)

    override fun draw(context: CanvasRenderingContext2D) {
        context.fillStyle = "rgb(255, 0, 0)"
        context.textBaseline = CanvasTextBaseline.HANGING
        context.font = "${lineHeight}px serif"
        context.lineWidth = 1.0

        if (text.isNotBlank()) {
            drawText(text, context)
        } else {
            context.fillStyle = "rgba(100, 100, 100, .7)"
            drawText("Type to add comment", context)
        }
    }

    private fun drawText(text: String, context: CanvasRenderingContext2D) =
            text.split("\n|\r\n".toRegex())
                    .flatMap { line ->
                        when {
                            context.measureText(line).width <= textWidth -> listOf(line)
                            else -> wrapLine(line, context)
                        }
                    }.forEachIndexed { i, wrappedLine ->
                            context.fillText(wrappedLine, rectangle.x + padding, rectangle.y + padding + (i * lineHeight))
                    }

    private fun wrapLine(line: String, context: CanvasRenderingContext2D): List<String> {
        val words = line.split(" ")
        var index = words.size

        while(index > 0 && context.measureText(words.subList(0, index).joinToString("", " ")).width > textWidth)
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

internal class TextBuilder(private val model: AnnotationModel, private val renderService: RenderService) : Builder {
    var startPoint: Point = Point(0.0, 0.0)
    var endPoint: Point = Point(0.0, 0.0)
    var mouseDown = false
    var editing = false
    var input: HTMLInputElement = document.createElement("input") as HTMLInputElement

    init {
        input.type = "text"
        input.style.transform = "scale(0)"
        input.style.position= "absolute"
        input.style.opacity= "0"
        input.style.asDynamic().pointerEvents = "none"
        input.style.asDynamic().userSelect = "none"
        input.oninput = ::textChanged
        input.onblur = ::textFinalized
        input.onkeypress = ::inputKeyPress
        document.body?.appendChild(input) ?: throw IllegalStateException("Could not create input for annotations")
    }

    override fun onMouseDown(point: Point) {
        if (!editing) {
            startPoint = point
            endPoint = point
        }
        mouseDown = true
    }

    override fun onMouseMove(point: Point) {
        if (mouseDown && !editing) {
            endPoint = point
            model.pendingDrawable = PendingText("", startPoint, endPoint)
            renderService.draw()
        }
    }

    override fun onMouseUp(point: Point) {
        if (mouseDown) {
            mouseDown = false
            if (!editing) {
                editing = true
                input.value = ""
                input.focus()
            }
            else {
                input.blur()
            }

        }
    }

    override fun onMouseLeave(point: Point) {
        if (mouseDown)
            reset()
    }

    override fun reset() {
        model.pendingDrawable = null
        mouseDown = false
        editing = false
        renderService.draw()
    }

    private fun textChanged(event: Event) {
        val value: String = event.target?.asDynamic().value
        model.pendingDrawable = PendingText(value, startPoint, endPoint)
        renderService.draw()
    }

    private fun textFinalized(event: Event) {
        val value: String = event.target?.asDynamic().value
        if (value.isNotBlank())
            model.drawables.add(Text(value, startPoint, endPoint))
        reset()
        renderService.draw()
    }

    private fun inputKeyPress(event: Event) {
        if (event is KeyboardEvent && event.keyCode == 13) //enter key
            input.blur()
    }
}