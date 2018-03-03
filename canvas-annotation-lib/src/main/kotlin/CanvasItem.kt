import org.w3c.dom.CanvasRenderingContext2D

internal interface CanvasItem: Drawable {
    val bounds: Rectangle
}

internal interface  Drawable {
    fun draw(context: CanvasRenderingContext2D)
}
