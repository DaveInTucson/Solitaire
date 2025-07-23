package s7n.solitaire.base.ui

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import javax.swing.JPanel

private const val CH_WIDTH = 21
private const val CH_HEIGHT = 21

/* This panel is used as a debug tool to provide a visual indication where mouse events are
 *
 */
class CrosshairPanel : JPanel() {

    init {
        isOpaque = false
        setHidden()
    }

    fun setHidden() {
        setLocation(-21, -21)
        setSize(0,0)
    }

    fun setPosition(p: Point) {
        setLocation(p.x - (CH_WIDTH-1)/2, p.y - (CH_HEIGHT-1)/2)
        setSize(CH_WIDTH, CH_HEIGHT)
        repaint()
    }

    override fun paintComponent(g: Graphics?) {
        if (g != null) {
            g.color = Color.RED
            g.drawLine((CH_WIDTH - 1) / 2, 0, (CH_WIDTH - 1) / 2, CH_HEIGHT)
            g.drawLine(0, (CH_HEIGHT - 1) / 2, CH_WIDTH, (CH_HEIGHT - 1) / 2)
        }
    }
}