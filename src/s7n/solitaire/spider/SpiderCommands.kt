package s7n.solitaire.spider

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.CardStackPanel
import java.util.*
import javax.swing.SwingUtilities
import kotlin.IllegalStateException

class SpiderDealDeckCommand: SolitaireCommand() {
    private var skipCount = 0

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            val deck = model.getCardStack(STACK_NAME_DECK)
            val tableaus = model.getCardStacks(STACKS_NAME_TABLEAUS)

            if (tableaus.find { it.isEmpty() } != null) return
            if (deck.isEmpty()) return
            tableaus.forEach {
                if (deck.isNotEmpty()) {
                    it.addTop(deck.getTopCard())
                    it.setTopFaceUp(true)
                } else skipCount++
            }

            success = true
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            val deck = model.getCardStack(STACK_NAME_DECK)
            val tableaus = model.getCardStacks(STACKS_NAME_TABLEAUS)

            tableaus.reversed().forEach {
                if (skipCount == 0) {
                    deck.addTop(it.getTopCard())
                    deck.setTopFaceUp(false)
                } else skipCount--
            }
        }
        finally {
            onComplete()
        }
    }
}

//--------------------------------------------------------------------------------
//
class SpiderAnimatedDealCommand(
    private val deckPanel: CardStackPanel,
    private val tableauPanels: List<CardStackPanel>,
    animationPanel: CardStackPanel) : AnimatedMoveCommand(animationPanel) {

    private var skipCount = 0
    private var tableauIndex = 0

    private fun dealCard(onComplete: (succeeded: Boolean) -> Unit) {
        if (deckPanel.cardStack.isNotEmpty()) {
            moveCard(deckPanel, tableauPanels[tableauIndex]) { success ->
                if (!success) throw IllegalStateException("Unexpected failure in SpiderAnimatedDealCommand!")
                tableauPanels[tableauIndex].cardStack.setTopFaceUp(true)
                tableauIndex++
                if (tableauIndex < tableauPanels.size)
                    SwingUtilities.invokeLater { dealCard(onComplete) }
                else
                    onComplete(true)
            }
        }
        else
            skipCount = tableauPanels.size - tableauIndex
    }

    private fun undealCard(onComplete: () -> Unit) {
        tableauIndex--
        if (tableauIndex >= 0) {
            moveCard(tableauPanels[tableauIndex], deckPanel) { success ->
                if (!success) throw IllegalStateException("unexpected failure in SpiderAnimatedDealCommand!")
                deckPanel.cardStack.setTopFaceUp(false)
                SwingUtilities.invokeLater { undealCard(onComplete) }
            }
        }
        else
            onComplete()
    }

    override fun doCommand(model: SolitaireModel, onComplete: (succeeded: Boolean) -> Unit) {
        if (deckPanel.cardStack.isEmpty()) {
            onComplete(false)
            return
        }
        if (tableauPanels.count { it.cardStack.isEmpty() } > 0) {
            onComplete(false)
            return
        }

        tableauIndex = 0
        dealCard(onComplete)
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        println("in undoCommand")
        tableauIndex = tableauPanels.size - skipCount
        undealCard(onComplete)

    }
}

//--------------------------------------------------------------------------------
//
data class CardLocation(val containingStack: CardStack, val index: Int, val faceUp: Boolean)

class SpiderCheat(private val cheatSuit: Suit): SolitaireCommand() {

    private val cheatMoveStack = Stack<CardLocation>()
    private val flipStack = Stack<CardStack>()

    private lateinit var targetGoal: CardStack

    private fun getCardLocation(deck: CardStack, tableaus: List<CardStack>, card: Card): CardLocation {
        for(i in tableaus.indices) {
            val index = tableaus[i].indexOfCard(card)
            if (index != -1)
                return CardLocation(tableaus[i], index, tableaus[i].cardStack[index].faceUp)
        }

        val index = deck.indexOfCard(card)
        if (index == -1) throw IllegalStateException("Can't find card $card!?")
        return CardLocation(deck, index, false)
    }

    override fun doCommand(model: SolitaireModel, onComplete: (Boolean) -> Unit) {
        var success = false
        try {
            targetGoal = model.getCardStacks(STACKS_NAME_GOALS).first { it.isEmpty() }

            val tableaus = model.getCardStacks(STACKS_NAME_TABLEAUS)
            val deck = model.getCardStack(STACK_NAME_DECK)
            for (rank in Rank.entries.reversed()) {
                val location = getCardLocation(deck, tableaus, Card(rank, cheatSuit))
                cheatMoveStack.push(location)
                val card = location.containingStack.getNthCard(location.index)
                card.faceUp = true
                targetGoal.addTop(card)
            }

            tableaus.forEach {
                if (it.setTopFaceUp(true)) flipStack.push(it)
            }
            model.cheatCount++
            success = true
        }
        finally { onComplete(success) }
    }

    override fun undoCommand(model: SolitaireModel, onComplete: () -> Unit) {
        try {
            while (flipStack.isNotEmpty())
                flipStack.pop().setTopFaceUp(false)

            while (cheatMoveStack.isNotEmpty()) {
                val cheatMove = cheatMoveStack.pop()
                val card = targetGoal.getTopCard()
                card.faceUp = cheatMove.faceUp
                cheatMove.containingStack.putNthCard(cheatMove.index, card)
            }

            model.cheatCount--
        }
        finally {
            onComplete()
        }
    }
}