package s7n.solitaire.pyramid

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.CardStackPanel

class PyramidDragCommand(
    private val sourcePanel: CardStackPanel,
    private val destinationPanel: CardStackPanel,
    private val dragPanel: CardStackPanel,
    private val goalPanel: CardStackPanel
    ): AnimatedMoveCommand(dragPanel) {
    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        require(dragPanel.cardStack.size == 1) { "dragPanel has ${dragPanel.cardStack.size} cards"}
        dragPanel.cardStack.add(destinationPanel.cardStack.getDragStack(1))
        moveCard(dragPanel, goalPanel) { success ->
            if (success && model is PyramidModel) {
                model.removeCovers(sourcePanel.cardStack)
                model.removeCovers(destinationPanel.cardStack)

            }
            onComplete(success)
        }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            require(goalPanel.cardStack.size >= 2) { "goal stack must have at least 2 cards!" }
            moveCard(goalPanel, destinationPanel) { success1 ->
                if (success1) {
                    moveCard(goalPanel, sourcePanel) { success2 ->
                        if (success2 && model is PyramidModel) {
                            model.addCovers(destinationPanel.cardStack)
                            model.addCovers(sourcePanel.cardStack)
                        }
                    }
                }
            }
        }
        finally {
            onComplete()
        }
    }
}

class PromoteKingToGoalCommand(
    private val sourcePanel: CardStackPanel,
    private val goalPanel: CardStackPanel,
    dragPanel: CardStackPanel) : AnimatedMoveCommand(dragPanel) {
    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        if (sourcePanel.cardStack.isEmpty() || sourcePanel.cardStack.peekTopCard().rank != Rank.King) {
            onComplete(false)
            return
        }

        moveCard(sourcePanel, goalPanel) { success ->
            if (success && model is PyramidModel)
                model.removeCovers(sourcePanel.cardStack)

            onComplete(success)
        }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        require(goalPanel.cardStack.peekTopCard().rank == Rank.King) { "goal top is not a King?!"}
        moveCard(goalPanel, sourcePanel) { success ->
            if (success && model is PyramidModel)
                model.addCovers(sourcePanel.cardStack)
            onComplete()
        }
    }
}

class PromoteKingToGoalOrDealCommand(
    private val deckPanel: CardStackPanel,
    private val wastePanel: CardStackPanel,
    private val goalPanel: CardStackPanel,
    dragPanel: CardStackPanel,
): AnimatedMoveCommand(dragPanel) {
    private var promotedKing = false

    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        var succeeded = false
        try {
            val deck = deckPanel.cardStack
            val waste = wastePanel.cardStack

            if (deckPanel.cardStack.isEmpty()) {
                deck.add(waste.getAllReversed())
                deck.setAllFaceUp(true)
                model.dealCount++
                succeeded = true
            } else if (deckPanel.cardStack.peekTopCard().rank == Rank.King) {
                moveCard(deckPanel, goalPanel) { success ->
                    promotedKing = success
                    onComplete(success)
                }
            } else {
                waste.addTop(deck.getTopCard())
                succeeded = true
            }
        }
        finally {
            onComplete(succeeded)
        }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            val deck = deckPanel.cardStack
            val waste = wastePanel.cardStack

            if (promotedKing) {
                require(goalPanel.cardStack.peekTopCard().rank == Rank.King) { "expecting goal top to be King" }
                moveCard(goalPanel, deckPanel) {
                    // empty
                }
            } else if (waste.isEmpty()) {
                waste.add(deck.getAllReversed())
                waste.setAllFaceUp(true)
                model.dealCount--
            } else {
                deck.addTop(waste.getTopCard())
            }
        }
        finally {
            onComplete()
        }
    }
}

class PyramidCheatCommand : SolitaireCommand() {

    private var tableauCount = 0
    private lateinit var checkPoint: PyramidModel

    private fun gatherRemainingCards(model: PyramidModel): CardStack {
        val remainingCards = CardStack("PyramidSCheatStack", CardStackDragSourceType.None)
        model.deck.cardStack.forEach { remainingCards.addTop(it) }
        model.waste.cardStack.forEach { remainingCards.addTop(it) }

        model.tableaus.forEach {tableauRow ->
            tableauRow.forEach { tableau ->
                if (tableau.isNotEmpty()) {
                    remainingCards.addTop(tableau.peekTopCard())
                    tableauCount++
                }
            }
        }

        return remainingCards
    }

    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        var success = false
        try {
            if (model is PyramidModel) {
                checkPoint = model.snapshot()
                val remainingCards = gatherRemainingCards(model)
                remainingCards.shuffle()
                model.tableaus.forEach { tableauRow ->
                    tableauRow.forEach { tableau ->
                        if (tableau.isNotEmpty()) {
                            tableau.getTopCard()
                            model.removeCovers(tableau)
                        }
                        if (tableauCount > 0) {
                            tableau.addTop(remainingCards.getTopCard())
                            model.addCovers(tableau)
                            tableauCount--
                            tableau.setAllFaceUp(true)
                        }
                    }
                }

                model.deck.clear()
                model.waste.clear()
                model.deck.add(remainingCards)
                model.deck.setAllFaceUp(true)
                model.cheatCount++
                success = true
            }
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            if (model is PyramidModel) {
                model.restoreFromSnapshot(checkPoint)
                model.cheatCount--
            }
        }
        finally {
            onComplete()
        }
    }

}