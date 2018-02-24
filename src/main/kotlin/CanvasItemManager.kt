

internal class CanvasItemManager(val backgroundItem: BackgroundItem, val model: AnnotationModel, val buttons: Buttons) {
    val orderedCanvasItems: List<CanvasItem> get() {
        val items = mutableListOf<CanvasItem>()
        items.add(backgroundItem)
        items.addAll(model.orderedCanvasItems)
        items.addAll(buttons.orderedCanvasItems)
        return items
    }
}
