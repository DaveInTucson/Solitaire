package s7n.solitaire.pyramid

import s7n.solitaire.base.CardStack
import s7n.solitaire.base.ui.CardStackPanel
import s7n.solitaire.base.ui.ImageManager
import java.awt.Graphics

/**
 * Normally when a CardStackPanel is empty, a rectangle is drawn to show its location.
 * (See ImageManager.drawCard). But for card stacks in the Pyramid tableau, they should
 * just "disappear" when they are empty. So this is a simple class to override the
 * paintComponent empty to only call ImageManager.drawCard when the stack is not empty.
 */
class PyramidTableauPanel(cardStack: CardStack): CardStackPanel(cardStack, false) {

    override fun paintComponent(g: Graphics?) {
        if (g != null && cardStack.isNotEmpty())
            ImageManager.drawCard(g, cardStack.peekTopCard(), 0, 0, this)
    }
}