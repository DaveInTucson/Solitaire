package s7n.solitaire.base

import s7n.solitaire.base.ui.AnimationManager
import s7n.solitaire.base.ui.CardStackPanel
import s7n.solitaire.base.ui.ImageManager

abstract class SolitaireCommand {

    abstract fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit)
    abstract fun undoCommand(model: SolitaireModel, onComplete: () -> Unit)
}

fun interface SolitaireCommandFactory {
    fun newCommand(): SolitaireCommand
}

//--------------------------------------------------------------------------------
//
class DealDeckToWaste: SolitaireCommand() {

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var succeeded = false
        try {
            val deck = model.getCardStack(STACK_NAME_DECK)
            val waste = model.getCardStack(STACK_NAME_WASTE)

            if (deck.isNotEmpty()) {
                waste.addTop(deck.getTopCard())
                waste.peekTopCard().faceUp = true
                succeeded = true
            } else if (waste.isNotEmpty()) {
                deck.add(waste.getAllReversed())
                deck.setAllFaceUp(false)
                model.dealCount++
                succeeded = true
            }

        }
        finally { onComplete(succeeded) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            val deck = model.getCardStack(STACK_NAME_DECK)
            val waste = model.getCardStack(STACK_NAME_WASTE)

            if (waste.isNotEmpty()) {
                deck.addTop(waste.getTopCard())
                deck.peekTopCard().faceUp = false
            } else {
                assert(waste.isEmpty())
                waste.add(deck.getAllReversed())
                waste.setAllFaceUp(true)
                model.dealCount--
            }
        }
        finally { onComplete() }
    }
}


//--------------------------------------------------------------------------------
//
class MoveSourceToGoal(private val sourceStack: CardStack): SolitaireCommand() {
    private var targetGoalStack: CardStack? = null
    private var flippedCard = false

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            if (sourceStack.isEmpty()) return
            val card = sourceStack.peekTopCard()

            val goals = model.getCardStacks(STACKS_NAME_GOALS)
            targetGoalStack = goals.find { it.accepts(card) }
            targetGoalStack?.apply {
                addTop(sourceStack.getTopCard())
                flippedCard = sourceStack.setTopFaceUp(true)
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
                    if (isNotEmpty() && flippedCard) peekTopCard().faceUp = false
                    addTop(card)
                }
            }
        }
        finally {
            onComplete()
        }
    }
}

//--------------------------------------------------------------------------------
//
abstract class AnimatedMoveCommand(
    private val dragPanel: CardStackPanel
): SolitaireCommand() {
    private var flippedCard = false

    protected fun moveCard(fromPanel: CardStackPanel, toPanel: CardStackPanel, onComplete: (Boolean) -> Unit) {

        dragPanel.cardStack.addTop(fromPanel.cardStack.getTopCard())
        // FIXME: probably better to have the face up/face down logic in the CardStack class
        // FIXME: except it also needs to be accessible here for undo purposes
        if (fromPanel.cardStack.isNotEmpty() && fromPanel.spreadDown)
            flippedCard = fromPanel.cardStack.setTopFaceUp(true)

        dragPanel.size = ImageManager.getCardDimension()
        AnimationManager.animate(dragPanel, fromPanel.getDragPoint(), toPanel.getDropPoint()) { success ->
            assert(dragPanel.cardStack.size > 0) { "dragPanel cardStack is empty!" }
            if (success) {
                toPanel.cardStack.add(dragPanel.cardStack)
                assert(dragPanel.cardStack.isNotEmpty()) { "dragPanel cartStack is empty!" }

                assert(dragPanel.cardStack.size > 0) { "dragPanel cardStack is empty!" }
                assert(toPanel.cardStack.isNotEmpty()) { "toPanel cardStack is empty!" }
            } else {
                val beforeSize = fromPanel.cardStack.size
                fromPanel.cardStack.add(dragPanel.cardStack)
                println("failed, from: ${fromPanel.cardStack}")
                assert(beforeSize + dragPanel.cardStack.size == fromPanel.cardStack.size)
            }
            dragPanel.setHidden()
            dragPanel.cardStack.clear()
            onComplete(success)
        }
    }

    protected fun unmoveCard(fromPanel: CardStackPanel, toPanel: CardStackPanel, onComplete: () -> Unit) {
        dragPanel.cardStack.addTop(toPanel.cardStack.getTopCard())
        toPanel.repaint()
        dragPanel.size = ImageManager.getCardDimension()
        if (flippedCard) fromPanel.cardStack.setTopFaceUp(false)
        AnimationManager.animate(dragPanel, toPanel.getDropPoint(), fromPanel.getDragPoint()) { success ->
            if (!success) throw IllegalStateException("Undo animation failed!?")
            fromPanel.cardStack.add(dragPanel.cardStack)
            fromPanel.repaint()
            dragPanel.cardStack.clear()
            dragPanel.setHidden()
            dragPanel.repaint()
            onComplete()
        }
    }
}

class AnimateSourceToGoal(
    private val sourcePanel: CardStackPanel,
    private val goalPanels: List<CardStackPanel>,
    dragPanel: CardStackPanel): AnimatedMoveCommand(dragPanel) {

    private var targetGoalPanel: CardStackPanel? = null

    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        if (sourcePanel.cardStack.isEmpty()) {
            onComplete(false)
            return
        }

        val card = sourcePanel.cardStack.peekTopCard()
        targetGoalPanel = goalPanels.find { it.cardStack.accepts(card) }
        if (targetGoalPanel == null) {
            onComplete(false)
            return
        }

        targetGoalPanel?.let {
            moveCard(sourcePanel, it) { success ->
                model.checkCardCount()
                onComplete(success)
            }
        }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        targetGoalPanel?.let {
            unmoveCard(sourcePanel, it, onComplete)
        }
    }
}



//--------------------------------------------------------------------------------
//
class DragCardsCommand(
    private val sourcePanel: CardStackPanel,
    private val destinationPanel: CardStackPanel,
    private val dragPanel: CardStackPanel) : SolitaireCommand() {

    private var flippedSource = false
    private var dragStackSize = 0

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            if (destinationPanel.cardStack.accepts(dragPanel.cardStack)) {
                dragStackSize = dragPanel.cardStack.size
                destinationPanel.cardStack.add(dragPanel.cardStack)
                flippedSource = sourcePanel.cardStack.setTopFaceUp(true)
                dragPanel.setHidden()
                dragPanel.cardStack.clear()
                model.checkCardCount()
                success = true
            }
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            val dragStack = destinationPanel.cardStack.getNCards(dragStackSize)
            if (flippedSource) sourcePanel.cardStack.setTopFaceUp(false)
            sourcePanel.cardStack.add(dragStack)
        }
        finally {
            onComplete()
        }
    }
}

//--------------------------------------------------------------------------------
//
class CheatCommand(private val sourceStack: CardStack, private val promotionIndex: Int): SolitaireCommand() {
    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            if (promotionIndex != -1) {
                sourceStack.moveToTop(promotionIndex)
                sourceStack.setTopFaceUp(true)
                model.cheatCount++
                success = true
            }
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            sourceStack.setTopFaceUp(false)
            sourceStack.moveTopToN(promotionIndex)
            model.cheatCount--
        }
        finally {
            onComplete()
        }
    }

}