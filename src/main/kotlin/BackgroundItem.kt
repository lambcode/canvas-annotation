import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLImageElement

/** This is the root canvas item and should cover the whole canvas **/
internal class BackgroundItem(private val image: HTMLImageElement,
                              override val bounds: Rectangle,
                              private val nodeToBuilder: Map<Mode, Builder>,
                              private val model: AnnotationModel,
                              private val renderService: RenderService
) : CanvasItem, MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseEnterHandler, MouseLeaveHandler {

    private var currentMode = Mode.RECTANGLE

    override fun draw(context: CanvasRenderingContext2D) {
        context.drawImage(image, 0.0, 0.0)
        builder.draw(context)
    }

    private inline fun <reified K>delegateMouseHandler(noinline function: CanvasMouseEvent.(K) -> Unit): CanvasMouseEvent.() -> Unit {
        return mouseEventHandler {
            val builder = this@BackgroundItem.builder
            if (builder is K)
                function(builder)
        }
    }

    override val onMouseDown = delegateMouseHandler<MouseDownHandler> { it.onMouseDown.invoke(this) }
    override val onMouseUp = delegateMouseHandler<MouseUpHandler> { it.onMouseUp.invoke(this) }
    override val onMouseMove = delegateMouseHandler<MouseMoveHandler> { it.onMouseMove.invoke(this) }
    override val onMouseEnter = delegateMouseHandler<MouseEnterHandler> { it.onMouseEnter.invoke(this) }
    override val onMouseLeave = delegateMouseHandler<MouseLeaveHandler> { it.onMouseLeave.invoke(this) }

    private val builder: Builder
        get() = nodeToBuilder[currentMode] ?: throw IllegalArgumentException("No builder registered for selected mode")

    fun changeMode(newMode: Mode) {
        builder.reset()
        this.currentMode = newMode
        renderService.draw()
    }

}

internal enum class Mode {
    RECTANGLE,
    FREEHAND,
    TEXT,
}
