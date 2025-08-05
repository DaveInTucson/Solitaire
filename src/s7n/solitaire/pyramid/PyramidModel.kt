package s7n.solitaire.pyramid

import s7n.solitaire.base.*

const val STACK_NAME_GOAL = "goal"

class PyramidModel: SolitaireModel() {

    val deck = PyramidCardStack(STACK_NAME_DECK)
    val waste = PyramidCardStack(STACK_NAME_WASTE)
    val goal  = CardStack(STACK_NAME_GOAL, CardStackDragSourceType.None)

    val tableau1 = (0..0).map { PyramidTableauCardStack(0, it) }
    val tableau2 = (0..1).map { PyramidTableauCardStack(1, it) }
    val tableau3 = (0..2).map { PyramidTableauCardStack(2, it) }
    val tableau4 = (0..3).map { PyramidTableauCardStack(3, it) }
    val tableau5 = (0..4).map { PyramidTableauCardStack(4, it) }
    val tableau6 = (0..5).map { PyramidTableauCardStack(5, it) }
    val tableau7 = (0..6).map { PyramidTableauCardStack(6, it) }

    val tableaus = arrayOf(tableau1, tableau2, tableau3, tableau4, tableau5, tableau6, tableau7)

    fun snapshot(): PyramidModel {
        val clone = PyramidModel()
        clone.deck.copy(deck)
        clone.waste.copy(waste)
        clone.goal.copy(goal)

        for (iRow in tableaus.indices) {
            for (iCol in tableaus[iRow].indices) {
                clone.tableaus[iRow][iCol].copy(tableaus[iRow][iCol])
            }
        }

        return clone
    }

    fun restoreFromSnapshot(snapshot: PyramidModel) {
        deck.copy(snapshot.deck)
        waste.copy(snapshot.waste)
        goal.copy(snapshot.goal)

        for (iRow in tableaus.indices) {
            for (iCol in tableaus[iRow].indices) {
                tableaus[iRow][iCol].copy(snapshot.tableaus[iRow][iCol])
            }
        }

        onChange()
    }

    override fun newGame() {
        cheatCount = 0
        dealCount = 1

        deck.makeFullDeck()
        deck.shuffle()
        deck.setAllFaceUp(true)

        waste.clear()
        goal.clear()

        tableaus.forEach { tableauRow ->
            tableauRow.forEach {
                it.clear()
                it.addTop(deck.getTopCard())
                it.setTopFaceUp(true)
                it.resetCover()
            }
        }
    }

    /**
     * Lower cards in the pyramid cover the one or two cards directly above each one. When a card
     * is removed from the pyramid, this method is called to notify its covered card(s) that it is
     * no longer covering them.
     */
    fun removeCovers(cardStack: CardStack) {
        if (cardStack is PyramidTableauCardStack) {
            val row = cardStack.row
            val col = cardStack.column
            if (row == 0) return
            if (col > 0) tableaus[row-1][col-1].uncover()
            if (col < row) tableaus[row-1][col].uncover()
        }
    }

    /**
     * This method is called when a card is replaced in the pyramid (via undo) to restore the covered
     * status of the card(s) it is covering
     */
    fun addCovers(cardStack: CardStack) {
        if (cardStack is PyramidTableauCardStack) {
            val row = cardStack.row
            val col = cardStack.column
            if (row == 0) return
            if (col > 0) tableaus[row-1][col-1].cover()
            if (col < row) tableaus[row-1][col].cover()
        }
    }

    override fun allGoalsFull(): Boolean {
        return goal.size == Suit.entries.size * Rank.entries.size
    }

    override fun getCardStack(name: String): CardStack {
        throw IllegalStateException("not used in Pyramid")
    }

    override fun getCardStacks(name: String): List<CardStack> {
        throw IllegalStateException("not used in Pyramid")
    }

    override fun getStatus(): String {
        if (gameIsWon()) return "You won!"
        var message = "$dealCount deal"
        if (dealCount != 1) message += "s"
        if (cheatCount > 0) {
            message += " | $cheatCount cheat"
            if (cheatCount != 1) message += "s"
        }
        return message
    }

    override fun getVictoryMessage(): String {
        if (!gameIsWon()) return "game is not won!?"
        var message = "You won with $dealCount deal";
        if (dealCount != 1) message += "s"
        if (cheatCount > 0) message += ", but you cheated ${getCheatCountMesage()}"
        return "$message."
    }
}