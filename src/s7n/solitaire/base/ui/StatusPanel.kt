package s7n.solitaire.base.ui

import java.awt.FlowLayout.LEFT
import javax.swing.BoxLayout
import javax.swing.BoxLayout.X_AXIS
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.BevelBorder

class StatusPanel: JPanel() {
    val statusLabel = JLabel("Status:")

    init {
        setBorder(BevelBorder(BevelBorder.LOWERED))
        layout = BoxLayout(this, X_AXIS)
        statusLabel.horizontalAlignment = LEFT
        add(statusLabel)
    }

    fun setStatus(status: String) {
        statusLabel.text = "Status: $status"
        repaint()
    }
}