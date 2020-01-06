import diagram.*
import libui.ktx.DrawContext
import libui.ktx.draw.Point
import libui.ktx.draw.Size
import libui.ktx.draw.fill
import libui.ktx.draw.stroke
import libui.uiDrawDefaultMiterLimit
import libui.uiDrawLineCapFlat
import libui.uiDrawLineJoinMiter
import kotlin.math.PI
import kotlin.math.min
import kotlin.random.Random

data class Box(
    var rectangle: Rectangle,
    var color: Int = colorDodgerBlue,
    var radius: Double = 5.0
)

class BoxElement(override val model: Box): DiagramElement<Box> {

    override val bounds = model.rectangle.expanded(10.0, 10.0)

    override fun display(context: DrawContext, diagram: DiagramContext) {
        context.fill(diagram.brush.solid(model.color)) {
            val rectangle = model.rectangle
            val radius = model.radius
            if (model.radius <= 0.0) {
                rectangle(rectangle)
            } else {
                val leftRadius = rectangle.topLeft.x + radius
                val topRadius = rectangle.topLeft.y + radius
                val rightRadius = rectangle.bottomRight.x - radius
                val bottomRadius = rectangle.bottomRight.y - radius

                figure(rectangle.topLeft.x, topRadius)
                arcTo(leftRadius, topRadius, radius, PI, PI /2.0)
                arcTo(rightRadius, topRadius, radius, PI *3.0/2.0, PI /2)
                arcTo(rightRadius, bottomRadius, radius, 0.0, PI /2)
                arcTo(leftRadius, bottomRadius, radius, PI /2, PI /2)
                closeFigure()
            }
        }
    }

    override fun hitTesting(zone: Rectangle, type: HitType, context: DiagramContext, result: MutableList<DiagramElement<Box>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun randomBoxes(size: Int = 10, maxWidth: Double = 2000.0, maxHeight: Double = 2000.0) = List(size) {
    val color = Random.nextInt()
    val radius = Random.nextDouble(0.0, 10.0)
    val left = Random.nextDouble(maxWidth - 20.0)
    val top = Random.nextDouble(maxHeight - 20.0)
    val width = Random.nextDouble(2 * radius, min(maxWidth - left, 100.0))
    val height = Random.nextDouble(2 * radius, min(maxHeight - top, 100.0))
    Box(Rectangle(Point(left, top), Size(width, height)), color, radius)
}
