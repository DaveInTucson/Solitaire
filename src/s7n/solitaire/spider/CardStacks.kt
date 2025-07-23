package s7n.solitaire.spider

import s7n.solitaire.base.*

//--------------------------------------------------------------------------------
//
class SpiderGoalStack(name: String): CardStack(name, CardStackDragSourceType.None) {

    override fun accepts(card: Card) = false

    private fun allSameSuitDecreasing(cards: CardStack): Boolean
    {
        for (i in 1..<cards.size) {
            if (cards[0].suit != cards[i].suit) return false
            if (cards[i-1].rank != cards[i].rank.next) return false
        }

        return true
    }

    override fun accepts(cards: CardStack): Boolean {
        if (isNotEmpty()) return false
        if (cards.size != Rank.entries.size) return false
        if (!allSameSuitDecreasing(cards)) return false
        return true
    }
}

fun isConsecutive(cardStack: CardStack, index: Int): Boolean {
    if (index >= cardStack.size-1) return false
    if (!cardStack[index].faceUp) return false
    if (cardStack[index].suit != cardStack[index+1].suit) return false
    if (cardStack[index].rank == Rank.Ace) return false
    return cardStack[index].rank == cardStack[index+1].rank.next
}

//--------------------------------------------------------------------------------
//
class SpiderTableauStack(name: String): CardStack(name, CardStackDragSourceType.Custom) {


    fun countSuits(suitCounts: Array<Int>) {
        cardStack.forEach {
            if (it.faceUp)
                suitCounts[it.suit.ordinal]++
        }
    }

    fun findRankIndex(rank: Rank): Int {
        for (i in cardStack.indices) {
            if (cardStack[i].rank == rank) return i
        }

        return -1
    }

    // draggable stack must be all the same suit and decreasing rank
    private fun canDragFrom(startIndex: Int): Boolean {
        for (i in startIndex..<cardStack.size-1) {
            if (cardStack[i].suit != cardStack[i+1].suit) return false
            if (cardStack[i].rank != cardStack[i+1].rank.next) return false
        }

        return true
    }

    override fun getDragStack(count: Int): CardStack {
        val dragStack = CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)

        // can't drag more cards than are in the stack
        if (count > size) return dragStack

        // can't drag a face down card
        val cardIndex = size - count
        if (!cardStack[cardIndex].faceUp) return dragStack

        // make sure stack is draggable (all same suit, decreasing rank)
        if (!canDragFrom(cardIndex)) return dragStack

        dragStack.add(getNCards(count))
        return dragStack
    }

    override fun accepts(cards: CardStack): Boolean {
        if (cards.isEmpty()) return false
        return accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return true
        val top = peekTopCard()
        return top.rank == card.rank.next
    }
}