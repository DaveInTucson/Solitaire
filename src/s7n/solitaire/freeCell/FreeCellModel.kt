package s7n.solitaire.freeCell

import s7n.solitaire.base.*

const val STACKS_NAME_FREE_CELLS = "freeCells"

class FreeCellModel: SolitaireModel() {
    // four goals, four free cells, 8 tableaus
    val goals = (1..4).map { GoalStack("goal $it", CardStackDragSourceType.None) }
    val freeCells = (1..4).map { FreeStack("free $it") }
    val tableaus = (1..8).map { FreeCellTableau("tableau $it", this) }

    val cardLocations = mutableMapOf<String, String>()

    init {
        //goals.forEach { it.addObserver { onChange() } }
        freeCells.forEach { it.addObserver { onChange() } }
        tableaus.forEach { it.addObserver { onChange() } }
    }

    override fun newGame() {
        dealCount = 0
        cheatCount = 0
        goals.forEach { it.clear() }
        freeCells.forEach { it.clear() }
        tableaus.forEach { it.clear() }

        val deck = CardStack(STACK_NAME_DECK, CardStackDragSourceType.None)
        deck.makeFullDeck()
        deck.shuffle()

        while (deck.isNotEmpty()) {
            tableaus.forEach {
                if (deck.isNotEmpty()) {
                    it.addTop(deck.getTopCard())
                    it.peekTopCard().faceUp = true
                }
            }
        }
    }
    override fun getCardStack(name: String): CardStack {
        throw IllegalArgumentException("FreeCell doesn't have any individual card stacks")
    }

    override fun getCardStacks(name: String): List<CardStack> {
        return when (name) {
            STACKS_NAME_GOALS -> goals
            STACKS_NAME_TABLEAUS -> tableaus
            STACKS_NAME_FREE_CELLS -> freeCells
            else -> throw IllegalArgumentException("unknown card stacks $name")
        }
    }

    fun getFreeCellCount(): Int {
        var freeTableauCount = 0
        var freeCellCount = 0

        // one free tableau spot can't be counted because it could be the drag destination
        tableaus.forEach { if (it.isEmpty()) freeTableauCount++ }
        if (freeTableauCount > 0) freeCellCount--

        freeCells.forEach { if (it.isEmpty()) freeCellCount++ }
        return freeTableauCount + freeCellCount
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
        //checkCardCount()
        if (gameIsWon()) {
            var message = "Game is won!"
            if (cheatCount > 0) message += " Cheats: $cheatCount"
            return message
        }

        val openCellCount = freeCells.count { it.isEmpty() }
        val openTableauCount = tableaus.count { it.isEmpty() }
        var message = "Open free cells: $openCellCount | Open tableaus: $openTableauCount"
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

    override fun checkCardCount() {
        var cardCount = 0
        tableaus.forEach { cardCount += it.size }
        freeCells.forEach { cardCount += it.size }
        goals.forEach { cardCount += it.size }
        if (cardCount == 52) {
            tableaus.forEach { tableau ->
                tableau.cardStack.forEach { cardLocations[it.toString()] = tableau.name } }
            freeCells.forEach { freeCell ->
                freeCell.cardStack.forEach { cardLocations[it.toString()] = freeCell.name } }
            goals.forEach { goal ->
                goal.cardStack.forEach { cardLocations[it.toString()] = goal.name }
            }
        }
        if (cardCount != 52) {
            println("cardCount=$cardCount!!!")
            val allCardSet : Set<Card> = (
                    tableaus.flatMap { it.cardStack } +
                            freeCells.flatMap { it.cardStack } +
                            goals.flatMap { it.cardStack }).toSet()
            val deck  = CardStack(STACK_NAME_DECK, CardStackDragSourceType.None)
            deck.makeFullDeck()
            val deckSet : Set<Card> = deck.cardStack.toSet()
            deckSet.forEach { it.faceUp = true }
            allCardSet.forEach { it.faceUp = true }
            val missing = deckSet - allCardSet
            println("Missing: $missing")
            missing.forEach { println("$it was last recorded at ${cardLocations[it.toString()]}") }

            println()
        }

    }
}