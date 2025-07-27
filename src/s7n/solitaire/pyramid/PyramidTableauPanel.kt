package s7n.solitaire.pyramid

import s7n.solitaire.base.CardStack
import s7n.solitaire.base.ui.CardStackPanel
import s7n.solitaire.base.ui.ImageManager
import s7n.solitaire.base.ui.TABLEAU_OFFSET
import java.awt.Graphics

class PyramidTableauPanel(cardStack: CardStack): CardStackPanel(cardStack, false) {
    init {
        isOpaque = false
    }

    override fun paintComponent(g: Graphics?) {
        if (g != null && cardStack.isNotEmpty())
            ImageManager.drawCard(g, cardStack.peekTopCard(), 0, 0, this)
    }
}