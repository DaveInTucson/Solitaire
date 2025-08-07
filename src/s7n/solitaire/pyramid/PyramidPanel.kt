package s7n.solitaire.pyramid

import s7n.solitaire.base.SolitaireModel
import s7n.solitaire.base.ui.*
import java.awt.Dimension

// TODO:
// * cheat

private const val PYRAMID_PANEL_HEIGHT = 550

class PyramidPanel(gameType: GameNames, statusPanel: StatusPanel): SolitairePanel(gameType, statusPanel) {

    private val model = PyramidModel()

    private val deckStackPanel = CardStackPanel(model.deck, false)
    private val wasteStackPanel = CardStackPanel(model.waste, false)
    private val goalStackPanel = CardStackPanel(model.goal, false)

    private val dragManager = PyramidDragManager(dragStackPanel, goalStackPanel)

    private val tableauStackPanels1 = model.tableau1.map { PyramidTableauPanel(it) }
    private val tableauStackPanels2 = model.tableau2.map { PyramidTableauPanel(it) }
    private val tableauStackPanels3 = model.tableau3.map { PyramidTableauPanel(it) }
    private val tableauStackPanels4 = model.tableau4.map { PyramidTableauPanel(it) }
    private val tableauStackPanels5 = model.tableau5.map { PyramidTableauPanel(it) }
    private val tableauStackPanels6 = model.tableau6.map { PyramidTableauPanel(it) }
    private val tableauStackPanels7 = model.tableau7.map { PyramidTableauPanel(it) }

    private val tableaus = arrayOf(
        tableauStackPanels1,
        tableauStackPanels2,
        tableauStackPanels3,
        tableauStackPanels4,
        tableauStackPanels5,
        tableauStackPanels6,
        tableauStackPanels7)

    init {
        preferredSize = Dimension(computePreferredWidth(8), PYRAMID_PANEL_HEIGHT)
        initialize()

        // By default, the child panels have their z order according to the order they're added to the parent.
        // For the case of the tableau, they are added from the bottom up, so lower rows physically cover the
        // rows above them.
        for (row in tableaus.size-1 downTo 0) {
            tableaus[row].forEachIndexed { column, tableau ->
                CardPositionManager.setTableauPosition(tableau, row, column)
                add(tableau)
            }
        }

        CardPositionManager.setDeckPosition(deckStackPanel)
        add(deckStackPanel)

        CardPositionManager.setWastePosition(wasteStackPanel)
        add(wasteStackPanel)

        CardPositionManager.setGoalPosition(goalStackPanel)
        add(goalStackPanel)

        addListeners()

        tableaus.forEach { tableauList ->
            tableauList.forEach { tableau ->
                tableau.setSingleClick {
                    PromoteKingToGoalCommand(tableau, goalStackPanel, animationPanel) }
            }
        }

        deckStackPanel.setSingleClick {
            PromoteKingToGoalOrDealCommand(deckStackPanel, wasteStackPanel, goalStackPanel, animationPanel)
        }

        model.newGame()
    }

    override fun getDragManager(): DragManager = dragManager

    override fun getModel(): SolitaireModel = model

    override fun onCheat() {
        val cheatCommand = PyramidCheatCommand()
        doCommand(cheatCommand) {
            // empty body
        }
    }

    override fun autoPromoteToGoal() {
        // empty body
    }
}