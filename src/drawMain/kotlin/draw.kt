import diagram.Diagram
import diagram.DiagramController
import libui.ktx.*

// and some colors
const val colorDodgerBlue = 0x1E90FF

fun main(args: Array<String>) = appWindow(
        title = "Draw test",
        width = 640,
        height = 480
) {
    val init = 10
    val boxes = randomBoxes(init)
    val diagram = Diagram(boxes) { it.map(::BoxElement) }

    val diagramController: DiagramController<Box> = DiagramController(diagram)
    val countBox: Spinbox = Spinbox(1, 2000).apply {
        value = init
        action {
            diagram.models = randomBoxes(value)
            diagramController.invalidate()
        }
    }

    val otherButton: Button = Button("New").apply {
        action {
            diagram.models = randomBoxes(countBox.value)
            diagramController.invalidate()
        }
    }

    val debugBounds: Checkbox = Checkbox("Show bounds").apply {
        value = false
        action { diagramController.debugBounds = value }
    }

    val zoomSlider: Slider = Slider(0, 100).apply {
        value = 50
        action { diagramController.zoom = (value*value)/(50.0*50.0) + 0.02 }
    }

    hbox {
        vbox {
            add(countBox)
            add(otherButton)
            add(debugBounds)
            add(zoomSlider)
        }

        stretchy = true
        add(diagramController.area)
    }
}