package s7n.solitaire.spider

import s7n.solitaire.base.*

class SpiderModel: SolitaireModel() {
    val deck = CardStack(STACK_NAME_DECK, CardStackDragSourceType.None)
    val goals = (1..8).map { SpiderGoalStack("goal stack $it")}
    val tableaus = (1..10).map { SpiderTableauStack("tableau stack $it") }

    init {
        // every move in this game is to and/or from the tableau, so there is no need to
        // observe the deck or goals for changes
        tableaus.forEach { it.addObserver { onChange() } }
        dragStack.addObserver { onChange() }
    }

    override fun newGame() {
        dealCount = 0
        cheatCount = 0

        deck.clear()
        goals.forEach { it.clear() }
        tableaus.forEach { it.clear() }

        deck.makeDoubleDeck()
        deck.shuffle()

        // 10 columns of 5 cards...
        for (row in 1..5) {
            for (element in tableaus) {
                element.addTop(deck.getTopCard())
            }
        }

        // plus one more for the first 4 columns, total of 54 cards dealt
        for (col in 0..3)
            tableaus[col].addTop(deck.getTopCard())

        tableaus.forEach {
            it.setAllFaceUp(false)
            it.setTopFaceUp(true)
        }
    }

    override fun getCardStack(name: String): CardStack {
        if (name == STACK_NAME_DECK)
            return deck

        throw IllegalArgumentException("Unknown stack name $name")
    }

    override fun getCardStacks(name: String): List<CardStack> {
        return when (name) {
            STACKS_NAME_GOALS -> goals
            STACKS_NAME_TABLEAUS -> tableaus
            else -> throw IllegalArgumentException("unknown stacks name $name")
        }
    }

    override fun getStatus(): String {
        if (gameIsWon()) {
            return when (cheatCount) {
                0 -> "You won!"
                1 -> "You won, but you cheated once"
                else -> "You won, but you cheated $cheatCount times"
            }
        }

        var consecutiveCount = 0
        (arrayOf(dragStack) + tableaus).forEach {
            for (i in it.cardStack.indices) {
                if (isConsecutive(it, i)) consecutiveCount++
            }
        }

        var message = "There are ${deck.size} cards in the deck. | Consecutive count: $consecutiveCount"
        if (cheatCount > 0) message += " | Cheats: $cheatCount"
        return message
    }

    override fun getVictoryMessage(): String {
        if (!gameIsWon()) return "Game is not won!"
        var message = "You won"
        if (cheatCount > 0) {
            message += ", but you cheated $cheatCount time"
            if (cheatCount > 1) message += "s"
        }
        message += "."
        return message
    }
}