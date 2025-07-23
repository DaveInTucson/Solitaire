package s7n.solitaire.freeCell

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.*
import java.awt.Dimension
import javax.swing.JOptionPane

const val FREE_CELL_PANEL_WIDTH = 850

class FreeCellPanel(gameTYpe: GameNames, statusPanel: StatusPanel): SolitairePanel(gameTYpe, statusPanel) {
    private val model = FreeCellModel()

    private val goalPanels = model.goals.map { CardStackPanel(it, false) }
    private val freePanels = model.freeCells.map { CardStackPanel(it, false) }
    private val tableauPanels = model.tableaus.map { CardStackPanel(it, true) }

    private val dragManager = KlondikeDragManager(dragStackPanel)

    init {
        preferredSize = Dimension(FREE_CELL_PANEL_WIDTH, PANEL_HEIGHT)

        initialize()

        var col = 0
        freePanels.forEach {
            setRowColumn(it, 0, col++)
            it.setDoubleClick { AnimateSourceToGoal(it, goalPanels, animationPanel) }
            add(it)
        }

        col++
        goalPanels.forEach {
            setRowColumn(it, 0, col++)
            add(it)
        }

        col = 0
        tableauPanels.forEach {
            setRowColumn(it, 1, col++)
            it.setDoubleClick { AnimateMoveToGoalOrFree(it, goalPanels, freePanels, animationPanel) }
            add(it)
        }

        addListeners()
        model.newGame()
    }

    override fun getDragManager(): DragManager = dragManager

    override fun getModel() = model

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
                doCommand(command)
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

    override fun autoPromoteToGoal() {
        val promotionRank = model.getLowestGoalRankNeeded()
        tableauPanels.forEach {
            if (it.cardStack.isNotEmpty() && it.cardStack.peekTopCard().rank == promotionRank) {
                val command = AnimateSourceToGoal(it, goalPanels, animationPanel)
                doCommand(command) {
                    autoPromoteToGoal()
                }
            }
        }

        freePanels.forEach {
            if (it.cardStack.isNotEmpty() && it.cardStack.peekTopCard().rank == promotionRank) {
                val command = AnimateSourceToGoal(it, goalPanels, animationPanel)
                doCommand(command) {
                    autoPromoteToGoal()
                }
            }
        }
    }
}