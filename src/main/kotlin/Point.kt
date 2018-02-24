import org.w3c.dom.CanvasRenderingContext2D

internal data class Point(val x: Double, val y: Double) {
    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())
}
