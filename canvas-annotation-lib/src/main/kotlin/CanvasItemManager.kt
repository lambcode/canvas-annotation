internal class CanvasItemManager(val backgroundItem: BackgroundItem, val model: AnnotationModel, val toolbar: Toolbar, val messageOverlay: SplashScreen) {

    val annotationItemsOnly: List<CanvasItem> get() {
        val items = mutableListOf<CanvasItem>()
        items.add(backgroundItem)
        items.addAll(model.orderedCanvasItems)
        return items
    }

    val orderedCanvasItems: List<CanvasItem> get() {
        val items = mutableListOf<CanvasItem>()
        items.addAll(annotationItemsOnly)
        items.add(messageOverlay)
        items.addAll(toolbar.orderedCanvasItems)
        return items
    }
}
