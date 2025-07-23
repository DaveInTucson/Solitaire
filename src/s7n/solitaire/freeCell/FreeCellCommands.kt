package s7n.solitaire.freeCell

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.CardStackPanel

class MoveTableauToGoalOrFree(private val sourceStack: CardStack): SolitaireCommand() {
    private var targetGoalStack: CardStack? = null

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            if (sourceStack.isEmpty()) return
            val card = sourceStack.peekTopCard()

            val goals = model.getCardStacks(STACKS_NAME_GOALS)
            val freeCells = model.getCardStacks(STACKS_NAME_FREE_CELLS)
            targetGoalStack = goals.find { it.accepts(card) }
            if (targetGoalStack == null)
                targetGoalStack = freeCells.find { it.isEmpty() }
            targetGoalStack?.apply {
                addTop(sourceStack.getTopCard())
                success = true
            }
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            assert(targetGoalStack != null)
            targetGoalStack?.apply {
                val card = this.getTopCard()
                sourceStack.apply {
                    addTop(card)
                }

            }
        }
        finally {
            onComplete()
        }
    }
}

class AnimateMoveToGoalOrFree(
    private val sourcePanel: CardStackPanel,
    private val goalPanels: List<CardStackPanel>,
    private val freePanels: List<CardStackPanel>,
    animationPanel: CardStackPanel
    ) : AnimatedMoveCommand(animationPanel) {

    private var toPanel: CardStackPanel? = null

    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        if (sourcePanel.cardStack.isEmpty()) {
            onComplete(false)
            return
        }

        val card = sourcePanel.cardStack.peekTopCard()
        toPanel = goalPanels.find { it.cardStack.accepts(card) }
        if (null == toPanel)
            toPanel = freePanels.find { it.cardStack.accepts(card) }
        if (null == toPanel) {
            onComplete(false)
            return
        }
        toPanel?.let {
            moveCard(sourcePanel, it, onComplete)
        }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        toPanel?.let {
            unmoveCard(sourcePanel, it, onComplete)
        }
    }

}