package s7n.scratch

import java.awt.*
import java.awt.event.*
import javax.swing.*

class AnimationPanel : JPanel() {
    var loopCount = 0

    var xPos = 50
    var yPos = 50
    var dx = 5
    var dy = 5


    fun runLoop() {
        loopCount++
        println("loopCount=$loopCount")
        var count = 0
        val timer = Timer(2) {
            if (count++ > 20) {
                (it.source as Timer).stop()
                rerun()
            }
            xPos += dx
            yPos += dy
            if (xPos > width - 50 || xPos < 0) dx *= -1
            if (yPos > height - 50 || yPos < 0) dy *= -1
            repaint()
        }
        timer.start()
    }

    fun rerun() {
        SwingUtilities.invokeLater {
            runLoop()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.RED
        g.fillOval(xPos, yPos, 50, 50)
    }
}

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Kotlin Swing Animation")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val panel = AnimationPanel()
        frame.add(panel)
        frame.setSize(400, 300)
        frame.isVisible = true
        panel.rerun()
    }
}