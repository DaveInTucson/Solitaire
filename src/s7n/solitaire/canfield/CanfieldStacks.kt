package s7n.solitaire.canfield

import s7n.solitaire.base.Card
import s7n.solitaire.base.CardStack
import s7n.solitaire.base.CardStackDragSourceType
import s7n.solitaire.base.Rank

class CanfieldGoalStack(name: String): CardStack(name, CardStackDragSourceType.Top) {
    var goalStartRank = Rank.Ace

    fun isFull() = size == 13

    override fun accepts(cards: CardStack): Boolean {
        if (cards.size != 1) return false
        return accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return card.rank == goalStartRank
        val top = peekTopCard()
        return top.suit == card.suit && top.rank.next == card.rank
    }
}

class CanfieldTableauStack(name: String): CardStack(name, CardStackDragSourceType.TopFaceUp) {
    override fun accepts(cards: CardStack): Boolean = cards.isNotEmpty() && accepts(cards[0])

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return true
        val top = peekTopCard()
        return top.suit.color != card.suit.color && top.rank == card.rank.next
    }
}