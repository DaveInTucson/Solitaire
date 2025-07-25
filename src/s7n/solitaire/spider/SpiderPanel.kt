package s7n.solitaire.spider

import s7n.solitaire.base.*
import s7n.solitaire.base.ui.*
import java.awt.Dimension

class SpiderPanel(gameType: GameNames, statusPanel: StatusPanel): SolitairePanel(gameType, statusPanel) {
    private val model = SpiderModel()

    private val deckPanel = CardStackPanel(model.deck, false)
    private val goalPanels = model.goals.map { CardStackPanel(it, false) }
    private val tableauPanels = model.tableaus.map { CardStackPanel(it, true) }

    private val dragManager = KlondikeDragManager(dragStackPanel)

    init {
        preferredSize = Dimension(computePreferredWidth(10), PANEL_HEIGHT)
        initialize()

        model.newGame()

        setRowColumn(deckPanel, 0, 0)
        deckPanel.setSingleClick { SpiderAnimatedDealCommand(deckPanel, tableauPanels, animationPanel) }
        add(deckPanel)

        var col = 2
        goalPanels.forEach {
            setRowColumn(it, 0, col++)
            add(it)
        }

        col = 0
        tableauPanels.forEach {
            setRowColumn(it, 1, col++)
            add(it)
        }

        addListeners()
    }

    override fun getDragManager(): DragManager = dragManager

    override fun getModel(): SolitaireModel = model

    override fun onCheat() {
        val allCards = tableauPanels.flatMap { it.cardStack.cardStack }
        if (allCards.isEmpty()) {
            println("tableaus are empty!")
            return
        }

        val mostRanks = allCards
            .groupBy { it.suit }
            .mapValues { (_, cards) -> cards.map { it.rank }.toSet().size
            }.maxByOrNull { it.value }

        println("most complete suit=${mostRanks?.key} ($mostRanks)")
        val cheatCommand = SpiderCheat(mostRanks!!.key)
        doCommand(cheatCommand)
    }

    override fun autoPromoteToGoal() {
        // empty body
    }
}