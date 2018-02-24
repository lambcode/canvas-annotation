import kotlinx.coroutines.experimental.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import kotlin.browser.document
import kotlin.coroutines.experimental.suspendCoroutine

@JsName("annotate")
fun annotate(base64EncodedImage: String): HTMLCanvasElement {
    val element = document.createElement("canvas") as HTMLCanvasElement
    val context = element.get2dContext() ?: throw IllegalStateException("Could not get render context")

    element.tabIndex = 0

    launch {
        val image = createImage(base64EncodedImage)
        val canvasArea = Rectangle(Point(0, 0), Point(image.naturalWidth, image.naturalHeight))
        element.width = image.naturalWidth
        element.height = image.naturalHeight
        val renderService = RenderService(context, canvasArea)

        val model = AnnotationModel()
        val modeToBuilder = createModeToBuilderMap(model, renderService)
        val backgroundItem = BackgroundItem(image, canvasArea, modeToBuilder, model, renderService)
        val buttons = Buttons(backgroundItem, renderService)

        val canvasItemManager = CanvasItemManager(backgroundItem, model, buttons)
        val eventService = EventService(element, canvasItemManager)

        renderService.canvasItemManager = canvasItemManager
        eventService.init()

        renderService.draw()
    }

    return element
}

suspend fun createImage(base64EncodedImage: String): HTMLImageElement =
        suspendCoroutine { cont ->
            val image = document.createElement("img") as HTMLImageElement
            image.onload = {
                cont.resume(image)
            }
            image.onerror = { _, _, _, _, _ ->
                cont.resumeWithException(IllegalArgumentException("The image could not be created with supplied string."))
            }
            //the src must be set after registering the callbacks to avoid missing the event
            image.src = base64EncodedImage
        }

internal fun HTMLCanvasElement.get2dContext() = this.getContext("2d") as CanvasRenderingContext2D?

internal interface Builder : Drawable {
    fun reset()
}

internal fun createModeToBuilderMap(model: AnnotationModel, renderService: RenderService): Map<Mode, Builder> = mapOf(
        Mode.RECTANGLE to RectangleBuilder(model, renderService),
        Mode.TEXT to TextBuilder(model, renderService)
)

