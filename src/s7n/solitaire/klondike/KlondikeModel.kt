package s7n.solitaire.klondike

import s7n.solitaire.base.*


class KlondikeModel: SolitaireModel() {
    val deck = CardStack(STACK_NAME_DECK, CardStackDragSourceType.None)
    val waste = CardStack(STACK_NAME_WASTE, CardStackDragSourceType.Top)

    val tableaus = (1..7).map { TableauStack("tableau $it", CardStackDragSourceType.TopFaceUp) }
    val goals = (1..4).map { GoalStack("goal $it", CardStackDragSourceType.Top) }

    init {
        //deck.addObserver { onChange() }
        waste.addObserver { onChange() }

        tableaus.forEach { it.addObserver { onChange() } }
        //goals.forEach { it.addObserver { onChange() } }
    }

    override fun getCardStack(name: String): CardStack {
        return when (name) {
            STACK_NAME_DECK -> deck
            STACK_NAME_WASTE -> waste
            else -> throw IllegalArgumentException("Unknown stack name $name")
        }
    }

    override fun getCardStacks(name: String): List<CardStack> {
        return when (name) {
            STACKS_NAME_GOALS -> goals
            STACKS_NAME_TABLEAUS -> tableaus
            else -> throw IllegalArgumentException("Unknown stacks name $name")
        }
    }

    override fun newGame() {
        cheatCount = 0
        dealCount = 1

        deck.makeFullDeck()
        deck.shuffle()
        waste.clear()
        goals.forEach { it.clear() }
        tableaus.forEach { it.clear() }

        for (row in tableaus.indices) {
            for (col in row..<tableaus.size) {
                tableaus[col].addTop(deck.getTopCard())
                tableaus[col].peekTopCard().faceUp = row == col
            }
        }
    }

    fun getLowestGoalRankNeeded(): Rank {
        var lowestRankNeeded = Rank.King
        goals.forEach {
            if (it.isEmpty())
                lowestRankNeeded = Rank.Ace
            else if (it.peekTopCard().rank.numericRank + 1 < lowestRankNeeded.numericRank)
                lowestRankNeeded = it.peekTopCard().rank.next
        }
        return lowestRankNeeded
    }

    override fun getStatus(): String {
        if (gameIsWon()) return "You won!"
        var message = "Deck size: ${deck.size} Waste size: ${waste.size} | Deals: $dealCount"
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

