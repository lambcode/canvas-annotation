import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

internal class EventService(element: HTMLCanvasElement) {

    private val eventHandlers = mutableListOf<EventHandler>()

    init {
        element.onmousedown = mouseEvent(::handleMouseDown)
        element.onmouseup = mouseEvent(::handleMouseUp)
        element.onmousemove = mouseEvent(::handleMouseMove)
        element.onmouseenter = mouseEvent(::handleMouseEnter)
        element.onmouseleave = mouseEvent(::handleMouseLeave)
    }

    private fun MouseEvent.getPoint(): Point = Point(this.offsetX, this.offsetY)

    private fun mouseEvent(callback: (MouseEvent) -> Unit): (Event) -> Unit = { event ->
        event.preventDefault()
        if (event is MouseEvent)
            callback.invoke(event)
        else
            throw IllegalArgumentException("Expected MouseEvent, but event was of different type")
    }

    private fun handleMouseDown(event: MouseEvent) =
            eventHandlers.forEach { it.onMouseDown(event.getPoint()) }

    private fun handleMouseUp(event: MouseEvent) =
        eventHandlers.forEach { it.onMouseUp(event.getPoint()) }

    private fun handleMouseMove(event: MouseEvent) =
            eventHandlers.forEach { it.onMouseMove(event.getPoint()) }

    private fun handleMouseEnter(event: MouseEvent) =
            eventHandlers.forEach { it.onMouseEnter(event.getPoint()) }

    private fun handleMouseLeave(event: MouseEvent) =
            eventHandlers.forEach { it.onMouseLeave(event.getPoint()) }

    fun addHandler(handler: EventHandler) = eventHandlers.add(handler)
}

internal interface EventHandler {
    fun onMouseDown(point: Point) { }
    fun onMouseUp(point: Point) { }
    fun onMouseMove(point: Point) { }
    fun onMouseEnter(point: Point) { }
    fun onMouseLeave(point: Point) { }
}
