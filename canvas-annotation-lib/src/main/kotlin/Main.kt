import kotlinx.coroutines.experimental.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import kotlin.browser.document
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.Promise

private const val DEFAULT_MESSAGE = "Use tools in toolbar above to annotate the image"

/** Prefer [annotateAsPromise] over this method. This method is provided for backwards compatibility if you need to support
 * browsers that do not support Promises (ie Internet Explorer)
 *
 * @param success is called with a new canvas element
 * @param error is called if the canvas could not be created
 */
@JsName("annotateAsCallback")
fun annotateAsCallback(base64EncodedImage: String,
                       success: (CanvasWrapper) -> Unit,
                       error: () -> Unit,
                       imageFileLocation: String = "",
                       message: String = DEFAULT_MESSAGE) {
    launch {
        try {
            success(annotate(base64EncodedImage, imageFileLocation, message))
        } catch (ex: Throwable) {
            console.error(ex)
            error()
        }
    }
}

/**
 * Create an enriched canvas element that allows users to highlight, add text, and more
 * This method is useful when integrating with non-kotlin libraries. It is recommended to
 * use [annotate] when using this library in another kotlin project to take full advantage of
 * coroutines
 */
@JsName("annotateAsPromise")
fun annotateAsPromise(base64EncodedImage: String, imageFileLocation: String = "", message: String = DEFAULT_MESSAGE): Promise<CanvasWrapper> {
    return Promise { resolve, reject ->
        launch {
            try {
                resolve(annotate(base64EncodedImage, imageFileLocation, message))
            }
            catch (ex: Exception) {
                reject(ex)
            }
        }
    }
}

/**
 * Create an enriched canvas element that allows users to highlight, add text, and more
 */
suspend fun annotate(base64EncodedImage: String, imageFileLocation: String = "", message: String = DEFAULT_MESSAGE): CanvasWrapper {
    val element = document.createElement("canvas") as HTMLCanvasElement
    val context = element.get2dContext() ?: throw IllegalStateException("Could not get render context")

    element.tabIndex = 0

    val image = createImage(base64EncodedImage)
    val canvasArea = Rectangle(Point(0, 0), Point(image.naturalWidth, image.naturalHeight))
    element.width = image.naturalWidth
    element.height = image.naturalHeight
    val renderService = RenderService(context, canvasArea)

    val messageOverlay = MessageOverlay(message, canvasArea, renderService)
    val model = AnnotationModel()
    val modeToBuilder = createModeToBuilderMap(model, renderService)
    val backgroundItem = BackgroundItem(image, canvasArea, modeToBuilder, model, renderService)
    val toolbar = Toolbar(backgroundItem, renderService, model)

    val canvasItemManager = CanvasItemManager(backgroundItem, model, toolbar, messageOverlay)
    val eventService = EventService(element, canvasItemManager)

    renderService.canvasItemManager = canvasItemManager
    eventService.init()
    toolbar.init(imageFileLocation)

    renderService.draw()

    return CanvasWrapper(element, renderService)
}

suspend fun createImage(base64EncodedImage: String): HTMLImageElement =
        suspendCoroutine { cont ->
            val image = document.createElement("img") as HTMLImageElement
            image.onload = {
                cont.resume(image)
            }
            image.onerror = { _, _, _, _, _ ->
                cont.resumeWithException(ImageCreationException("The image could not be created with supplied string."))
            }
            //the src must be set after registering the callbacks to avoid missing the event
            image.src = base64EncodedImage
        }

class ImageCreationException(message: String) : Exception(message)

internal fun HTMLCanvasElement.get2dContext() = this.getContext("2d") as CanvasRenderingContext2D?

internal interface Builder : Drawable {
    fun reset()
}

internal fun createModeToBuilderMap(model: AnnotationModel, renderService: RenderService): Map<Mode, Builder> = mapOf(
        Mode.RECTANGLE to RectangleBuilder(model, renderService),
        Mode.TEXT to TextBuilder(model, renderService)
)

