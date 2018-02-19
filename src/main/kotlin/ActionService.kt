import org.w3c.dom.CanvasRenderingContext2D
import kotlin.browser.window

internal class ActionService(var currentMode: Mode,
                             val builders: Map<Mode, Builder>,
                             private val changeModeCallback: (Mode) -> Unit = {}
) : EventHandler {

    override fun onMouseDown(point: Point) = builder.onMouseDown(point)

    override fun onMouseUp(point: Point) = builder.onMouseUp(point)

    override fun onMouseMove(point: Point) = builder.onMouseMove(point)

    override fun onMouseEnter(point: Point) {
        builder.onMouseMove(point)
    }

    override fun onMouseLeave(point: Point) {
        builder.onMouseLeave(point)
    }

    private val builder: Builder
        get() = builders.get(currentMode) ?: throw IllegalArgumentException("No builder registered for selected mode")

    fun changeMode(newMode: Mode) {
        builder.reset()
        this.currentMode = newMode
        changeModeCallback(newMode)
    }

}

internal enum class Mode {
    RECTANGLE,
    FREEHAND,
    TEXT,
}
