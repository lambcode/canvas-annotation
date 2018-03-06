import org.w3c.dom.HTMLCanvasElement

/**
 * Object to return when creating a canvas-annotation html canvas element. This object contains the [canvas] as well as
 * a [annotatedImage] property to pull the image (minus all the toolbar etc) off of the canvas
 */
@Suppress("MemberVisibilityCanBePrivate")
class CanvasWrapper internal constructor(val canvas: HTMLCanvasElement, private val renderService: RenderService) {

    val annotatedImage: String get() {
            renderService.drawForExternal()
            val value = canvas.toDataURL()
            renderService.draw()
            return value
    }
}
