package s7n.solitaire.base.ui

import java.awt.Point
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.abs
import kotlin.math.sqrt

private const val ANIMATION_TIMER_DELAY = 1
private const val ANIMATION_MOVE_SIZE = 40


object AnimationManager {
    private val timer = Timer(ANIMATION_TIMER_DELAY) { doAnimationBody() }

    private lateinit var movingPanel: JPanel
    private lateinit var onAnimationDone: ((Boolean) -> Unit)

    private var fromPoint = Point(0, 0)
    private var toPoint = Point(0, 0)

    private var currentX: Double = 0.0
    private var currentY: Double = 0.0
    private var deltaX: Double = 0.0
    private var deltaY: Double = 0.0
    private var loopCount = 0

    private fun computeAnimationParameters() {
        val xLength = toPoint.x - fromPoint.location.x
        val yLength = toPoint.y - fromPoint.location.y
        if (xLength == 0 && yLength == 0) throw IllegalArgumentException("fromPoint == toPoint!")

        val length = sqrt((xLength * xLength + yLength * yLength).toDouble())

        deltaX = (xLength * ANIMATION_MOVE_SIZE) / length
        deltaY = (yLength * ANIMATION_MOVE_SIZE) / length
        currentX = fromPoint.x.toDouble()
        currentY = fromPoint.y.toDouble()
    }

    fun animate(panel: JPanel, fromPoint: Point, toPoint: Point, onAnimationDone: (Boolean) -> Unit) {
        if (timer.isRunning) throw IllegalStateException("animation timer is already animating!")
        this.movingPanel = panel
        this.fromPoint = fromPoint
        this.toPoint = toPoint
        this.onAnimationDone = onAnimationDone
        loopCount = 0
        panel.setLocation(fromPoint.x, fromPoint.y)
        computeAnimationParameters()
        timer.start()
    }

    private fun doAnimationBody() {
        try {
            loopCount++
            if (loopCount > 100) {
                println("too many loops!")
                timer.stop()
                onAnimationDone(false)
                return
            }
            currentX += deltaX
            currentY += deltaY
            movingPanel.setLocation(currentX.toInt(), currentY.toInt())
            movingPanel.repaint()
            if (abs(movingPanel.location.x - toPoint.x) <= ANIMATION_MOVE_SIZE &&
                abs(movingPanel.location.y - toPoint.y) <= ANIMATION_MOVE_SIZE
            ) {
                try {
                    timer.stop()
                } finally {
                    onAnimationDone(true)
                }
            }
        }
        catch (e: Exception) {
            println("caught $e")
        }
    }
}