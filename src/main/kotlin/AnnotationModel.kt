import org.w3c.dom.CanvasRenderingContext2D

internal class AnnotationModel : Drawable {
    val drawables = mutableListOf<Drawable>()
    var pendingDrawable: Drawable? = null

    override fun draw(context: CanvasRenderingContext2D) {
        drawables.forEach { it.draw(context) }
        pendingDrawable?.draw(context)
    }
}