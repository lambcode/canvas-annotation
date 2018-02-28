import org.w3c.dom.CanvasRenderingContext2D
import kotlin.browser.window

internal class RenderService(private val context: CanvasRenderingContext2D,
                             private val canvasArea: Rectangle
) {
    var canvasItemManager: CanvasItemManager? = null

    fun draw() {
        window.requestAnimationFrame { internalDraw(true) }
    }

    private fun internalDraw(drawAll: Boolean) {
        context.clearRect(canvasArea)
        val items =
                if (drawAll) canvasItemManager?.orderedCanvasItems
                else canvasItemManager?.annotationItemsOnly
        items?.forEach {
            context.save()
            it.draw(context)
            context.restore()
        }
    }

    fun drawForExternal() = internalDraw(false)
}