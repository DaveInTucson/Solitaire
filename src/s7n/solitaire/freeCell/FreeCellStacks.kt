package s7n.solitaire.freeCell

import s7n.solitaire.base.*

class FreeStack(name: String): CardStack(name, CardStackDragSourceType.Top) {
    override fun accepts(cards: CardStack): Boolean {
        return isEmpty() && cards.size == 1
    }

    override fun accepts(card: Card): Boolean {
        return isEmpty()
    }
}

class FreeCellTableau(name: String, private val model: FreeCellModel): CardStack(name, CardStackDragSourceType.Custom) {
    override fun setTopFaceUp(faceUp: Boolean): Boolean {
        if (!faceUp) throw IllegalArgumentException("attempt to place face down card on tableau")
        return super.setTopFaceUp(faceUp)
    }

    private fun canDragTopNCards(n: Int): Boolean {
        if (size < n) return false
        if (n == 1) return true

        // there has to be enough free cells to move the stack
        if (model.getFreeCellCount() < n-1) return false

        // the stack must be alternating colors and decreasing rank
        for (i in size-n..<size-1) {
            if (cardStack[i].suit.color == cardStack[i+1].suit.color) return false
            if (cardStack[i].rank.prev != cardStack[i+1].rank) return false
        }

        return true
    }

    override fun getDragStack(count: Int): CardStack {
        val dragStack = CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)
        if (canDragTopNCards(count))
            dragStack.add(getNCards(count))

        return dragStack
    }

    override fun accepts(cards: CardStack): Boolean {
        if (isEmpty()) return true
        return accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return true
        if (card.rank == Rank.King) return false
        val topCard = peekTopCard()
        return topCard.suit.color != card.suit.color && topCard.rank == card.rank.next
    }
}