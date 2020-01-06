package diagram

import libui.ktx.DrawArea
import libui.ktx.DrawContext
import libui.ktx.ScrollingArea
import libui.ktx.draw.*
import libui.uiDrawDefaultMiterLimit
import libui.uiDrawLineCapFlat
import libui.uiDrawLineJoinMiter
import libui.uiDrawPathNewFigure
import platform.darwin.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable
import kotlin.reflect.KClass

fun Size.expanded(deltaX: Double = 0.0, deltaY: Double = 0.0) = Size(width+deltaX, height+deltaY)
fun Point.moveBy(deltaX: Double = 0.0, deltaY: Double = 0.0) = Point(x+deltaX, y+deltaY)

data class Rectangle(val topLeft: Point, val size: Size) {
    val bottomRight = Point(topLeft.x + size.width, topLeft.y + size.height)

    fun expanded(deltaX: Double, deltaY: Double = deltaX) = Rectangle(
            topLeft.moveBy(-deltaX/2, -deltaY/2),
            size.expanded(deltaX/2, deltaY/2)
    )
}

fun Path.figure(p: Point) = figure(p.x, p.y)
fun Path.rectangle(r: Rectangle) = this.rectangle(r.topLeft.x, r.topLeft.y, r.size.width, r.size.height)

/** Does [this] rectangle intersect with [other] ? */
fun Rectangle.intersect(other: Rectangle): Boolean =
    topLeft.x <= other.bottomRight.x && other.topLeft.x <= other.bottomRight.x &&
    topLeft.y <= other.bottomRight.y && other.topLeft.y <= bottomRight.y

enum class HitType {
    Selection, HoverEnter, HoverExist
}

interface DiagramContext {
    val area: DrawArea
    val brush: Brush

    val debugBounds: Boolean
    val zoom: Double
}

interface Displayable {
    val bounds: Rectangle
    fun display(context: DrawContext, diagram: DiagramContext)
}

interface DiagramElement<M>: Displayable {
    val model: M

    // TODO Should the list be returned ? I don't think so since the hitTesting can be hierarchical thought creating too much instances ?
    fun hitTesting(zone: Rectangle, type: HitType, context: DiagramContext, result: MutableList<DiagramElement<M>>)

    fun click(point: Point, context: DiagramContext) {}
}

class Diagram<M>(
    initialModels: List<M>, val build: (List<M>) -> List<DiagramElement<M>>
) {
    var models: List<M> by observable(initialModels) { _, _, _ ->
        rebuild()
    }

    var elements: List<DiagramElement<M>> = build(models)
    var bounds: Rectangle = computeBounds()

    private fun computeBounds(): Rectangle {
        var left = Double.MAX_VALUE
        var right = Double.MIN_VALUE
        var top = Double.MAX_VALUE
        var bottom = Double.MIN_VALUE
        elements.forEach {
            left = min(left, it.bounds.topLeft.x)
            right = max(right, it.bounds.topLeft.y)
            top = min(top, it.bounds.bottomRight.x)
            bottom = max(bottom, it.bounds.bottomRight.y)
        }
        return Rectangle(Point(left, top), Size(right-left, bottom - top))
    }

    fun rebuild() {
        elements = build(models)
        bounds = computeBounds()
    }

}

class DiagramController<M>(
    val diagram: Diagram<M>
): DiagramContext {

    private fun <T>invalidateOnChange(initialValue: T) = observable(initialValue) {
        _, old, new -> if (old != new) invalidate()
    }

    override val area: ScrollingArea = ScrollingArea(
        diagram.bounds.size.width.roundToInt(),
        diagram.bounds.size.height.roundToInt()
    ).apply {
        draw { display(this) }

        mouseEvent {
            if (it.Count == 1) {
                val worldX = this@apply.
                println("One Click ${it.X},${it.Y}")
            }
        }
    }

    private val boundsStroke = area.stroke {
        Cap = uiDrawLineCapFlat
        Join = uiDrawLineJoinMiter
        Thickness = 1.0
        MiterLimit = uiDrawDefaultMiterLimit
    }

    override val brush: Brush = area.brush()

    override var debugBounds: Boolean by invalidateOnChange(false)

    override var zoom by invalidateOnChange(1.0)

    fun display(context: DrawContext) {
        val worldWidth = zoom * diagram.bounds.size.width
        val worldHeight = zoom * diagram.bounds.size.height
        area.setSize(worldWidth.roundToInt(), worldHeight.roundToInt())

        // TODO save state
        // apply world transformation
        context.transform { scale(0.0, 0.0, zoom, zoom) }
        diagram.elements.forEach {
            if (debugBounds) {
                val bounds = it.bounds
                context.stroke(brush.solid(0xFF0000), boundsStroke) {
                    //figure(bounds.topLeft)
                    rectangle(bounds)
                }
            }
            it.display(context, this)
        }
        // TODO restore state
    }

    inline fun <reified T: Any> findElementUnder(zone: Rectangle, type: HitType): T? {
        val result = mutableListOf<DiagramElement<M>>()
        for (element in diagram.elements) {
            element.hitTesting(zone, type, this, result)
            result.filterIsInstance<T>().firstOrNull()?.let { return it }
        }
        return null
    }

    fun invalidate(zone: Rectangle? = null) {
        // TODO invalidate areas for optimization using zone
        area.redraw()
    }
}
