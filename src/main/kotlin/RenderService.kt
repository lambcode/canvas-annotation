import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLImageElement
import kotlin.browser.window

internal class RenderService(private val model: AnnotationModel,
                             private val context: CanvasRenderingContext2D,
                             private val image: HTMLImageElement,
                             private val width: Int,
                             private val height: Int
) {

    fun draw() {
        window.requestAnimationFrame { drawInternal() }
    }

    private fun drawInternal() {
        context.clearRect(0.0, 0.0, width.toDouble(), height.toDouble())
        context.drawImage(image, 0.0, 0.0)
        model.draw(context)
    }
}