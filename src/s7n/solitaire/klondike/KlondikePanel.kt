package s7n.solitaire.klondike

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.*
import java.awt.Dimension
import javax.swing.JOptionPane

class KlondikePanel(gameType: GameNames, statusPanel: StatusPanel): SolitairePanel(gameType, statusPanel) {
    private val model = KlondikeModel()

    private val wastePanel = CardStackPanel(model.waste, false)
    private val deckPanel = CardStackPanel(model.deck, false)
    private val goalPanels = model.goals.map { CardStackPanel(it, false) }
    private val tableauPanels = model.tableaus.map { CardStackPanel(it, true) }

    private val dragManager = KlondikeDragManager(dragStackPanel)

    override fun getModel() = model

    override fun getDragManager(): DragManager = dragManager

    init {
        preferredSize = Dimension(computePreferredWidth(7), PANEL_HEIGHT)
        initialize()

        model.newGame()

        var col = 0
        goalPanels.forEach {
            setRowColumn(it, 0, col++)
            add(it)
        }

        setRowColumn(wastePanel, 0, 5)
        wastePanel.setDoubleClick { AnimateSourceToGoal(wastePanel, goalPanels, animationPanel) }

        add(wastePanel)

        setRowColumn(deckPanel, 0, 6)
        deckPanel.setSingleClick { DealDeckToWaste() }
        deckPanel.onDoubleClick = deckPanel.onSingleClick
        add(deckPanel)

        col = 0
        tableauPanels.forEach {
            setRowColumn(it, 1, col++)
            it.setDoubleClick { AnimateSourceToGoal(it, goalPanels, animationPanel) }
            add(it)
        }

        addListeners()
    }

    override fun autoPromoteToGoal() {
            val lowestRankNeeded = model.getLowestGoalRankNeeded()
            promoteToGoal(lowestRankNeeded)
    }

    private fun promoteToGoal(rank: Rank) {

        tableauPanels.forEach {
            if (it.cardStack.isNotEmpty() && it.cardStack.peekTopCard().rank == rank) {
                val command = AnimateSourceToGoal(it, goalPanels, animationPanel)
                doCommand(command) {
                    autoPromoteToGoal()
                }
            }
        }

        if (wastePanel.cardStack.isNotEmpty() && wastePanel.cardStack.peekTopCard().rank == rank) {
            val command = AnimateSourceToGoal(wastePanel, goalPanels, animationPanel)
            doCommand(command) {
                autoPromoteToGoal()
            }
        }
    }

    override fun onCheat() {
        val rank = model.getLowestGoalRankNeeded()
        var index = -1
        val cheatTableau = tableauPanels.find {
            index = it.cardStack.indexOfRank(rank)
            index != -1
        }

        if (index != -1 && cheatTableau != null) {
            if (index < cheatTableau.cardStack.size -1) {
                val command = CheatCommand(cheatTableau.cardStack, index)
                doCommand(command) {
                    // empty body
                }
            }
            else {
                JOptionPane.showMessageDialog(
                    this,
                    "You can put $rank card on the goal stack yourself",
                )
            }
        }
        else
            println("didn't find rank $rank?!")
    }
}