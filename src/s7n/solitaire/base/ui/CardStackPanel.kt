package s7n.solitaire.base.ui

import s7n.solitaire.base.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import javax.swing.JPanel

const val TABLEAU_OFFSET = 20

class CardStackPanel(val cardStack: CardStack, public val spreadDown: Boolean): JPanel() {

    var onSingleClick : SolitaireCommandFactory? = null
    var onDoubleClick : SolitaireCommandFactory? = null

    fun setSingleClick(scf: SolitaireCommandFactory) { onSingleClick = scf }
    fun setDoubleClick(scf: SolitaireCommandFactory) { onDoubleClick = scf }

    init {
        preferredSize = Dimension(ImageManager.getCardWidth(), ImageManager.getCardHeight())
        setSize()
        background = Color(FELT_GREEN_RGB)

        cardStack.addObserver {
            setSize()
            repaint()
        }
    }

    private fun setSize() {
        var height = ImageManager.getCardHeight()
        if (spreadDown && cardStack.size > 1) height += TABLEAU_OFFSET * (cardStack.size - 1)
        setSize(ImageManager.getCardWidth(), height)
    }

    fun onClick(clickCount: Int): SolitaireCommand? {
        return if (clickCount == 1) onSingleClick?.newCommand()
            else if (clickCount > 1) onDoubleClick?.newCommand()
            else null
    }

    fun setHidden() {
        setLocation(-ImageManager.getCardWidth(), ImageManager.getCardHeight())
        setSize(0, 0)
    }

    private fun pointToCardIndex(point: Point): Int {
        val yOffset = point.y - bounds.y
        if (yOffset < (cardStack.size-1) * TABLEAU_OFFSET)
            return yOffset / TABLEAU_OFFSET
        return cardStack.size -1
    }

    fun getDragStack(point: Point): CardStack {
        var count = 1
        if (spreadDown && cardStack.size > 1) {
            val cardIndex = pointToCardIndex(point)
            count = cardStack.size - cardIndex
        }
        return cardStack.getDragStack(count)
    }

    fun setDragPanelPosition(dragPanel: CardStackPanel, point: Point) {
        var dragY = location.y
        var dragHeight = ImageManager.getCardHeight()
        if (spreadDown) {

            dragY += cardStack.size * TABLEAU_OFFSET
            dragHeight += (dragPanel.cardStack.size - 1) * TABLEAU_OFFSET
        }
        dragPanel.setLocation(location.x, dragY)
        dragPanel.setSize(width, dragHeight)
    }

    /** This is used to animate card motion. It gives the starting point of the animation,
     * where to set the animation panel at the start of the animation.
     */
    fun getDragPoint(): Point {
        val dragPoint = location
        if (spreadDown)
            dragPoint.y += TABLEAU_OFFSET * (cardStack.size-1)
        return dragPoint
    }

    /** THis is used to animate card motion. It gives the ending point of the animation,
     * where the animation panel should finish it's motion at the end of the animation.
     */
    fun getDropPoint(): Point {
        val dropPoint = location
        if (spreadDown)
            dropPoint.y += TABLEAU_OFFSET * cardStack.size
        return dropPoint
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (g != null) {
            if (cardStack.isEmpty())
                ImageManager.drawRect(g)
            else if (!spreadDown)
                ImageManager.drawCard(g, cardStack.peekTopCard(), 0, 0, this)
            else {
                for (i in cardStack.indices)
                    ImageManager.drawCard(g, cardStack[i], 0, i * TABLEAU_OFFSET,  this)

            }
        }
    }
}