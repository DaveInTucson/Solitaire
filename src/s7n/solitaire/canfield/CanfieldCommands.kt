package s7n.solitaire.canfield

import s7n.solitaire.base.CardStack
import s7n.solitaire.base.STACKS_NAME_GOALS
import s7n.solitaire.base.SolitaireCommand
import s7n.solitaire.base.SolitaireModel

class MoveReserveToTableau : SolitaireCommand() {
    lateinit var tableauStack: CanfieldTableauStack

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            val reserve = model.getCardStack(STACK_NAME_RESERVE)
            if (reserve.isEmpty()) return
            if (!tableauStack.accepts(reserve.peekTopCard())) return
            tableauStack.addTop(reserve.getTopCard())
            reserve.setTopFaceUp(true)
            success = true
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            assert(tableauStack.isNotEmpty())
            val reserve = model.getCardStack(STACK_NAME_RESERVE)
            reserve.setTopFaceUp(false)
            reserve.addTop(tableauStack.getTopCard())
        }
        finally {
            onComplete()
        }
    }
}

class MoveReserveToGoal: SolitaireCommand() {
    var targetGoal: CardStack? = null

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            val reserve = model.getCardStack(STACK_NAME_RESERVE)
            val goals = model.getCardStacks(STACKS_NAME_GOALS)
            if (reserve.isEmpty()) return
            targetGoal = goals.find { it.accepts(reserve.peekTopCard()) }
            if (targetGoal != null) {
                targetGoal?.addTop(reserve.getTopCard())
                reserve.setTopFaceUp(true)
                success = true
            }
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            assert(targetGoal != null)
            val reserve = model.getCardStack(STACK_NAME_RESERVE)
            targetGoal?.let {
                reserve.setTopFaceUp(false)
                reserve.addTop(it.getTopCard())
            }
        }
        finally {
            onComplete()
        }
    }

}