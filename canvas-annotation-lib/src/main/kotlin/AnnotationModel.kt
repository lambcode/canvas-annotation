internal class AnnotationModel {
    val orderedCanvasItems = mutableListOf<CanvasItem>()

    fun undoLastChange() {
        if (orderedCanvasItems.isNotEmpty())
            orderedCanvasItems.removeAt(orderedCanvasItems.lastIndex)
    }
}