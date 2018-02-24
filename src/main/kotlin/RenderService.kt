import org.w3c.dom.CanvasRenderingContext2D
import kotlin.browser.window

internal class RenderService(private val context: CanvasRenderingContext2D,
                             private val canvasArea: Rectangle
) {
    var canvasItemManager: CanvasItemManager? = null

    fun draw() {
        window.requestAnimationFrame { drawInternal() }
    }

    private fun drawInternal() {
        context.clearRect(canvasArea)
        canvasItemManager?.orderedCanvasItems?.forEach {
            context.save()
            it.draw(context)
            context.restore()
        }
    }
}