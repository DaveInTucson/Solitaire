package s7n.solitaire.pyramid

import s7n.solitaire.base.Card
import s7n.solitaire.base.CardStack
import s7n.solitaire.base.CardStackDragSourceType
import s7n.solitaire.base.STACK_NAME_DRAG

open class PyramidCardStack(title: String): CardStack(title, CardStackDragSourceType.Custom) {

    override fun getDragStack(count: Int): CardStack {
        val dragStack = CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)
        if (count == 1 && cardStack.size > 0) {
            dragStack.addTop(getTopCard())
        }

        return dragStack
    }

    override fun accepts(cards: CardStack): Boolean {
        return cards.size == 1 && accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (cardStack.size == 0) return false
        return card.rank.numericRank + peekTopCard().rank.numericRank == 13
    }
}

class PyramidTableauCardStack(val row: Int, val column: Int): PyramidCardStack("tableau stack (row=$row col=$column)") {
    private var coverCount = initialCoverCount()

    private fun initialCoverCount() = if (row < 6) 2 else 0

    override fun getDragStack(count: Int): CardStack {
        if (coverCount != 0) return CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)
        return super.getDragStack(count)
    }

    override fun accepts(card: Card): Boolean {
        if (coverCount != 0) return false
        return super.accepts(card)
    }

    fun uncover() {
        assert(coverCount > 0) { "uncover called when coverCount is 0!"}
        coverCount--
    }

    fun cover() {
        assert(coverCount < 2) { "cover called when coverCount is $coverCount!"}
        coverCount++
    }
    fun resetCover() {
        coverCount = initialCoverCount()
    }
}