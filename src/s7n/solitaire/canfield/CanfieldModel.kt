package s7n.solitaire.canfield

import s7n.solitaire.base.*

const val RESERVE_SIZE = 13
const val STACK_NAME_RESERVE = "reserve"

class CanfieldModel: SolitaireModel() {
    val deck = CardStack(STACK_NAME_DECK, CardStackDragSourceType.None)
    val waste = CardStack(STACK_NAME_WASTE, CardStackDragSourceType.Top)
    val reserve = ReserveStack(STACK_NAME_RESERVE)

    val tableaus = (1..4).map { CanfieldTableauStack("tableau $it") }
    val goals = (1..4).map { CanfieldGoalStack("goal $it") }

    init {
        //deck.addObserver { onChange() }
        waste.addObserver { onChange() }
        reserve.addObserver { onChange() }

        tableaus.forEach { it.addObserver { onChange() } }
        //goals.forEach { it.addObserver { onChange() } }
    }

    override fun getCardStack(name: String): CardStack {
        return when(name) {
            STACK_NAME_DECK -> deck
            STACK_NAME_WASTE -> waste
            STACK_NAME_RESERVE -> reserve
            else -> throw IllegalArgumentException("unknown stack name $name")
        }
    }

    override fun getCardStacks(name: String): List<CardStack> {
        return when (name) {
            STACKS_NAME_GOALS -> goals
            STACKS_NAME_TABLEAUS -> tableaus
            else -> throw IllegalArgumentException("unknown stacks name $name")
        }
    }

    override fun newGame() {
        dealCount = 0
        cheatCount = 0

        deck.makeFullDeck()
        deck.shuffle()
        waste.clear()
        reserve.clear()

        tableaus.forEach { it.clear() }
        goals.forEach { it.clear() }

        tableaus.forEach {
            it.addTop(deck.getTopCard())
            it.setTopFaceUp(true)
        }

        var goalStartRank = Rank.Ace
        goals[0].apply {
            addTop(deck.getTopCard())
            setTopFaceUp(true)
            goalStartRank = peekTopCard().rank
        }

        goals.forEach { it.goalStartRank = goalStartRank }

        reserve.apply {
            for(i in 1..RESERVE_SIZE) {
                addTop(deck.getTopCard())
                setTopFaceUp(false)
            }
            setTopFaceUp(true)
        }

        onChange()
    }

    private fun compareRanks(r1: Rank, r2: Rank) : Int {
        val goalStartRank = goals[0].goalStartRank
        var n1 = r1.numericRank
        var n2 = r2.numericRank
        if (n1 < goalStartRank.numericRank) n1 += Rank.King.numericRank
        if (n2 < goalStartRank.numericRank) n2 += Rank.King.numericRank
        return n1 - n2
    }

    fun getLowestGoalRankNeeded(): Rank {
        var lowestRank = goals[0].goalStartRank.prev
        goals.forEach {
            if (!it.isFull()) {
                if (it.isEmpty())
                    lowestRank = it.goalStartRank
                else if (compareRanks(it.peekTopCard().rank.next, lowestRank) < 0)
                    lowestRank = it.peekTopCard().rank.next
            }
        }

        return lowestRank
    }

    override fun getStatus(): String {
        if (gameIsWon()) {
            var message = "Game is won! Redeals: $dealCount"
            if (cheatCount > 0) message += " Cheats: $cheatCount"
            return message
        }

        val baseRank = goals[0].goalStartRank
        var message =
            "Base rank: $baseRank | Deck size: ${deck.size} | Waste size: ${waste.size} | Reserve size: ${reserve.size} | Redeals: $dealCount"
        if (cheatCount > 0) message += " | Cheats: $cheatCount"
        return message
    }

    override fun getVictoryMessage(): String {
        if (!gameIsWon()) return "Game is not won!"
        var message = "You won with $dealCount deal"
        if (dealCount > 1) message += "s"
        if (cheatCount > 0) {
            message += ", but you cheated ${getCheatCountMesage()}"
        }
        return "$message."
    }
}