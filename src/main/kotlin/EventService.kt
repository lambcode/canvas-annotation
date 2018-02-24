import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

internal class EventService(private val element: HTMLCanvasElement, private val canvasItemManager: CanvasItemManager) {

    var lastPoint = Point(0, 0)

    fun init() {
        element.onmousedown = convertToMouseEvent(::handleMouseDown)
        element.onmouseup = convertToMouseEvent(::handleMouseUp)
        element.onmousemove = convertToMouseEvent(::handleMouseMove)
        element.onmouseenter = convertToMouseEvent(::handleMouseEnter)
        element.onmouseleave = convertToMouseEvent(::handleMouseLeave)
    }

    private fun convertToMouseEvent(callback: (MouseEvent) -> Unit): (Event) -> Unit = { event ->
        event.preventDefault()
        if (event is MouseEvent)
            callback.invoke(event)
        else
            throw IllegalArgumentException("Expected MouseEvent, but event was of different type")
    }

    private inline fun <reified T> sendActionToAllAtPoint(point: Point, action: (T, CanvasMouseEvent) -> Unit) {
        val predicate: (CanvasItem) -> Boolean = { it.bounds.containsPoint(point) }
        sendActionToAllConditionally(point, predicate, action)
    }

    private inline fun <reified T> sendActionToAllConditionally(point: Point, predicate: (CanvasItem) -> Boolean, action: (T, CanvasMouseEvent) -> Unit) {
        val canvasMouseEvent = CanvasMouseEvent(point)
        for (canvasItem in canvasItemManager.orderedCanvasItems.reversed()) {
            // Only send the action to this canvas item if it handles the event and the predicate returns true
            if (canvasItem is T && predicate(canvasItem))
                action(canvasItem, canvasMouseEvent)

            // Stop bubbling if requested
            if (!canvasMouseEvent.propagate)
                break
        }
    }

    private fun handleMouseDown(event: MouseEvent) = sendActionToAllAtPoint<MouseDownHandler>(event.offsetPoint) { handler, event -> handler.onMouseDown(event) }

    private fun handleMouseUp(event: MouseEvent) = sendActionToAllAtPoint<MouseUpHandler>(event.offsetPoint) { handler, event -> handler.onMouseUp(event) }

    private fun handleMouseMove(event: MouseEvent) {
        val currentPoint = event.offsetPoint
        val enterPredicate: (CanvasItem) -> Boolean = { it.bounds.containsPoint(currentPoint) && !it.bounds.containsPoint(lastPoint) }
        val leavePredicate: (CanvasItem) -> Boolean = { it.bounds.containsPoint(lastPoint) && !it.bounds.containsPoint(currentPoint) }

        // MouseMove
        sendActionToAllAtPoint<MouseMoveHandler>(currentPoint) { handler, event -> handler.onMouseMove(event) }
        // MouseEnter
        sendActionToAllConditionally<MouseEnterHandler>(currentPoint, enterPredicate) { handler, event -> handler.onMouseEnter(event) }
        // MouseLeave
        sendActionToAllConditionally<MouseLeaveHandler>(currentPoint, leavePredicate) { handler, event -> handler.onMouseLeave(event) }

        lastPoint = currentPoint
    }

    private fun handleMouseEnter(event: MouseEvent) = sendActionToAllAtPoint<MouseEnterHandler>(event.offsetPoint) { handler, event -> handler.onMouseEnter(event) }

    private fun handleMouseLeave(event: MouseEvent) {
        val currentPoint = event.offsetPoint
        val leavePredicate: (CanvasItem) -> Boolean = { it.bounds.containsPoint(lastPoint) && !it.bounds.containsPoint(currentPoint) }
        sendActionToAllConditionally<MouseLeaveHandler>(event.offsetPoint, leavePredicate) { handler, event -> handler.onMouseLeave(event) }

        lastPoint = event.offsetPoint
    }
}

internal val MouseEvent.offsetPoint: Point get() = Point(this.offsetX, this.offsetY)

internal class CanvasMouseEvent(var point: Point) {
    var propagate = true

    fun preventPropagation() {
        propagate = false
    }
}

internal fun mouseEventHandler(action: CanvasMouseEvent.() -> Unit): CanvasMouseEvent.() -> Unit {
    return action
}

internal interface MouseDownHandler {
    val onMouseDown: CanvasMouseEvent.() -> Unit
}

internal interface MouseUpHandler {
    val onMouseUp: CanvasMouseEvent.() -> Unit
}

internal interface MouseMoveHandler {
    val onMouseMove: CanvasMouseEvent.() -> Unit
}

internal interface MouseEnterHandler {
    val onMouseEnter: CanvasMouseEvent.() -> Unit
}

internal interface MouseLeaveHandler {
    val onMouseLeave: CanvasMouseEvent.() -> Unit
}

