package s7n.solitaire.base

// Maybe add a parameter indicating type of change (add, remove , update)?
fun interface ChangeObserver {
    fun onChange()
}

enum class CardStackDragSourceType {
    None, Top, TopFaceUp, Custom
}

open class CardStack(val name: String, private val dragSourceType: CardStackDragSourceType) {
    private val observers = ArrayList<ChangeObserver>()

    val cardStack = ArrayList<Card>()

    val indices
        get() = cardStack.indices

    val size
        get() = cardStack.size

    fun addObserver(observer: ChangeObserver) {
        observers.add(observer)
    }

    private fun onChange() {
        observers.forEach { it.onChange() }
    }

    fun addTop(card: Card) {
        cardStack.add(card)
        onChange()
    }

    fun add(cardStack: CardStack) {
        this.cardStack.addAll(cardStack.cardStack)
        onChange()
    }

    fun getTopCard(): Card {
        val card = cardStack.removeLast()
        onChange()
        return card
    }

    fun peekTopCard(): Card = cardStack.last()

    fun isEmpty() = cardStack.isEmpty()
    fun isNotEmpty() = cardStack.isNotEmpty()
    fun clear() {
        cardStack.clear()
        onChange()
    }


    fun setAllFaceUp(faceUp: Boolean) {
        cardStack.forEach { it.faceUp = faceUp }
        onChange()
    }

    open fun setTopFaceUp(faceUp: Boolean) : Boolean {
        if (isNotEmpty() && peekTopCard().faceUp != faceUp) {
            peekTopCard().faceUp = faceUp
            onChange()
            return true
        }
        return false
    }

    fun indexOfRank(rank: Rank): Int {
        for (i in cardStack.indices) {
            if (cardStack[i].rank == rank)
                return i
        }

        return -1
    }

    fun moveToTop(index: Int) {
        cardStack.add(cardStack.removeAt(index))
        onChange()
    }

    fun moveTopToN(n: Int) {
        val card = getTopCard()
        cardStack.add(n, card)
    }

    fun getNthCard(n: Int): Card {
        val card =  cardStack.removeAt(n)
        onChange()
        return card
    }

    fun putNthCard(n: Int, card: Card) {
        cardStack.add(n, card)
        onChange()
    }

    fun makeFullDeck() {
        makeNDecks(1)
    }

    fun makeDoubleDeck() {
        makeNDecks(2)
    }

    fun makeNDecks(n: Int) {
        cardStack.clear()
        for (i in 1..n) {
            for(suit in Suit.entries) {
                for (rank in Rank.entries) {
                    addTop(Card(rank, suit))
                }
            }
        }

        onChange()
    }

    private fun reverse() {
        cardStack.reverse()
        onChange()
    }

    fun getAllReversed(): CardStack {
        val allReversed = CardStack(name, dragSourceType)
        allReversed.add(this)
        allReversed.reverse()
        this.clear()
        return allReversed
    }

    fun shuffle() {
        cardStack.shuffle()
        onChange()
    }

    operator fun get(i: Int): Card = cardStack[i]

    fun getNCards(n: Int): CardStack {
        val nCards = CardStack("n cards", CardStackDragSourceType.None)
        for (i in 1..n)
            nCards.addTop(cardStack.removeLast())

        nCards.reverse()
        onChange()
        return nCards
    }

    open fun accepts(cards: CardStack) = false
    open fun accepts(card: Card) = false

    open fun getDragStack(count: Int): CardStack {
        val dragStack = CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)
        if (size == 0) return dragStack

        when (dragSourceType) {
            CardStackDragSourceType.Top -> {
                if (count == 1) dragStack.addTop(getTopCard())
            }
            CardStackDragSourceType.TopFaceUp -> {
                if (size >= count && cardStack[size - count].faceUp) {
                    dragStack.add(getNCards(count))
                }
            }
            CardStackDragSourceType.Custom ->
                throw IllegalArgumentException("You must override CardStack:getDragStock on a Custom drag source type")

            CardStackDragSourceType.None -> {
                // do nothing
            }
        }

        return dragStack
    }

    override fun toString(): String {
        return "$name(" + cardStack.joinToString() + ")"
    }

    fun indexOfCard(card: Card): Int {
        for (i in indices)
            if (cardStack[i].suit == card.suit && cardStack[i].rank == card.rank) return i

        return -1
    }
}

class GoalStack(name: String, sourceType: CardStackDragSourceType): CardStack(name, sourceType) {
    override fun accepts(cards: CardStack): Boolean {
        if (cards.size != 1) return false
        return accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return card.rank == Rank.Ace
        val top = peekTopCard()
        return top.suit == card.suit && top.rank.next == card.rank
    }
}

class AlwaysAcceptsStack(name: String, sourceType: CardStackDragSourceType): CardStack(name, sourceType) {
    override fun accepts(card: Card) = true
    override fun accepts(cards: CardStack) = true
}

class TableauStack(name: String, sourceType: CardStackDragSourceType): CardStack(name, sourceType) {
    override fun accepts(cards: CardStack): Boolean {
        assert(cards.size > 0)
        return accepts(cards[0])
    }

    override fun accepts(card: Card): Boolean {
        if (isEmpty()) return card.rank == Rank.King
        val top = peekTopCard()
        return top.suit.color != card.suit.color &&
                top.rank == card.rank.next

    }
}

class ReserveStack(name: String): CardStack(name, CardStackDragSourceType.Top)