import diagram.Diagram
import diagram.DiagramController
import diagram.DiagramContext
import libui.ktx.*
import libui.ktx.draw.*
import kotlin.math.roundToInt

// diagram margins
const val xoffLeft = 20.0
const val yoffTop = 20.0
const val xoffRight = 20.0
const val yoffBottom = 20.0
const val pointRadius = 5.0

// and some colors
const val colorWhite = 0xFFFFFF
const val colorBlack = 0x000000
const val colorDodgerBlue = 0x1E90FF

fun graphWidth(clientWidth: Double): Double = clientWidth - xoffLeft - xoffRight
fun graphHeight(clientHeight: Double): Double = clientHeight - yoffTop - yoffBottom

fun main(args: Array<String>) = appWindow(
        title = "Draw test",
        width = 640,
        height = 480
) {
    val init = 10
    val boxes = randomBoxes(init)
    val diagram = Diagram(boxes) { it.map(::BoxElement) }

    hbox {
        val diagramController: DiagramController<Box> = DiagramController(diagram)

        val countBox: Spinbox = spinbox(1, 2000) {
            value = init
            action {
                diagram.models = randomBoxes(value)
                diagramController.invalidate()
            }
        }

        val otherButton: Button = button("New") {
            action {
                diagram.models = randomBoxes(countBox.value)
                diagramController.invalidate()
            }
        }

        val debugBounds: Checkbox = checkbox("Show bounds") {
            value = false
            action { diagramController.debugBounds = value }
        }

        val zoomSlider: Slider = slider(0, 100) {
            value = 50
            action { diagramController.zoom = (value*value)/(50.0*50.0) + 0.02 }
        }


        vbox {
            add(countBox)
            add(otherButton)
            add(debugBounds)
            add(zoomSlider)
        }

        add(diagramController.area)

    }
}