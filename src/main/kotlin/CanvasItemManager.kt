

internal class CanvasItemManager(val backgroundItem: BackgroundItem, val model: AnnotationModel, val toolbar: Toolbar) {
    val orderedCanvasItems: List<CanvasItem> get() {
        val items = mutableListOf<CanvasItem>()
        items.add(backgroundItem)
        items.addAll(model.orderedCanvasItems)
        items.addAll(toolbar.orderedCanvasItems)
        return items
    }
}
