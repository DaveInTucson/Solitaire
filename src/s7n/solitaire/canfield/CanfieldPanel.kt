package s7n.solitaire.canfield

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.CardStackPanel
import s7n.solitaire.base.ui.*
import java.awt.Dimension
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

private const val CANFIELD_PANEL_HEIGHT = 600

class CanfieldPanel(gameType: GameNames, statusPanel: StatusPanel): SolitairePanel(gameType, statusPanel) {
    private val model = CanfieldModel()

    private val goalPanels = model.goals.map { CardStackPanel(it, false) }
    private val wastePanel = CardStackPanel(model.waste, false)
    private val deckPanel = CardStackPanel(model.deck, false)
    private val tableauPanels = model.tableaus.map { CardStackPanel(it, true) }
    private val reservePanel = CardStackPanel(model.reserve, true)

    private val dragManager = KlondikeDragManager(dragStackPanel)

    override fun getModel() = model

    override fun getDragManager() = dragManager

    init {
        preferredSize = Dimension(computePreferredWidth(7), CANFIELD_PANEL_HEIGHT)
        initialize()

        model.newGame()

        setRowColumn(deckPanel, 0, 0)
        deckPanel.setSingleClick { DealDeckToWaste() }
        deckPanel.setDoubleClick { DealDeckToWaste() }
        add(deckPanel)

        setRowColumn(wastePanel, 0, 1)
        wastePanel.setDoubleClick { AnimateSourceToGoal(wastePanel, goalPanels, animationPanel) }

        add(wastePanel)

        var col = 3
        goalPanels.forEach {
            setRowColumn(it, 0, col++)
            add(it)
        }

        setRowColumn(reservePanel, 1, 1)
        reservePanel.setDoubleClick { AnimateSourceToGoal(reservePanel, goalPanels, animationPanel) }
        add(reservePanel)

        col = 3
        tableauPanels.forEach {
            setRowColumn(it, 1, col++)
            it.setDoubleClick { AnimateSourceToGoal(it, goalPanels, animationPanel) }
            add(it)
        }

        addListeners()

    }

    override fun doPostCommand() {

        if (model.reserve.isNotEmpty()) {
            val openTableauPanel = tableauPanels.find { it.cardStack.isEmpty() }
            if (openTableauPanel != null) {
                val command = AnimateSourceToGoal(reservePanel, tableauPanels, animationPanel)
                doCommand(command) {
                    SwingUtilities.invokeLater { doPostCommand() }
                }
            }
        }
    }

    override fun onCheat() {
        if (model.reserve.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There is no reserve, you don't need to cheat!")
            return
        }

        val promotionRank = model.getLowestGoalRankNeeded()
        if (model.reserve.peekTopCard().rank == promotionRank) {
            JOptionPane.showMessageDialog(this, "You can move the $promotionRank to the goal by yourself!")
            return
        }

        val promotionIndex = model.reserve.indexOfRank(promotionRank)
        if (promotionIndex == -1) {
            JOptionPane.showMessageDialog(this, "No $promotionRank found in reserve!")
            return
        }

        val command = CheatCommand(model.reserve, promotionIndex)
        println("promotionIndex = $promotionIndex")
        doCommand(command)
    }

    override fun autoPromoteToGoal() {

        val promotionRank = model.getLowestGoalRankNeeded()
        val promotionTableau = tableauPanels.find {
            it.cardStack.isNotEmpty() && it.cardStack.peekTopCard().rank == promotionRank
        }
        if (promotionTableau != null) {
            val command = AnimateSourceToGoal(promotionTableau, goalPanels, animationPanel)
            doCommand(command) { SwingUtilities.invokeLater { autoPromoteToGoal() } }
        }

        if (model.waste.isNotEmpty()) {
            if (model.waste.peekTopCard().rank == promotionRank) {
                val command = AnimateSourceToGoal(wastePanel, goalPanels, animationPanel)
                doCommand(command) { SwingUtilities.invokeLater { autoPromoteToGoal() } }
            }
        }

        if (model.reserve.isNotEmpty()) {
            if (model.reserve.peekTopCard().rank == promotionRank) {
                val command = AnimateSourceToGoal(reservePanel, goalPanels, animationPanel)
                doCommand(command) { SwingUtilities.invokeLater { autoPromoteToGoal() } }
            }
            else if (!dragManager.isDragging){
                val command = AnimateSourceToGoal(reservePanel, tableauPanels, animationPanel)
                doCommand(command) { SwingUtilities.invokeLater { autoPromoteToGoal() } }
            }
        }
    }
}
