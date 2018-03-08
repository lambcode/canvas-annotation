import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HANGING
import org.w3c.dom.MIDDLE
import kotlin.math.min

internal class SplashScreen(messageOverride: String?, canvasArea: Rectangle, val renderService: RenderService) : CanvasItem, MouseDownHandler {

    var visible = true
    var message = messageOverride ?: "Use tools in toolbar above to annotate the image"

    override val bounds: Rectangle = canvasArea

    val overlayBox = Rectangle(
            Point(canvasArea.width * .2, canvasArea.height * .4),
            Point(canvasArea.width * .8, canvasArea.height * .6))

    override fun draw(context: CanvasRenderingContext2D) {
        if (visible && message.isNotBlank()) {
            context.fillStyle = "rgba(0, 0, 0, .6)"
            context.fillRect(overlayBox)

            context.fillStyle = "rgb(255, 255, 255)"
            context.font = "25px serif"
            context.textBaseline = CanvasTextBaseline.MIDDLE
            val paddingWidth = overlayBox.width * .10
            val paddingHeight = overlayBox.height * .10
            val widthAdjust = context.measureText(message).width
            val adjustedFontSize = min(
                    25 * ((overlayBox.width - paddingWidth) / widthAdjust),
                    overlayBox.height - paddingHeight)
            context.font = "${adjustedFontSize}px serif"
            val width = context.measureText(message).width
            context.fillText(message, overlayBox.x + (overlayBox.width - width) / 2, overlayBox.y + (overlayBox.height / 2))
        }
    }

    override val onMouseDown = mouseEventHandler {
        visible = false
        renderService.draw()
    }
}