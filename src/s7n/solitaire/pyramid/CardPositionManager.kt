package s7n.solitaire.pyramid

import s7n.solitaire.base.ui.*

object CardPositionManager {

    fun setDeckPosition(deckStackPanel: CardStackPanel) {
        setRowColumn(deckStackPanel, 0, 0)
    }

    fun setWastePosition(wasteStackPanel: CardStackPanel) {
        setRowColumn(wasteStackPanel, 0, 1)
    }

    fun setGoalPosition(goalStackPanel: CardStackPanel) {
        setRowColumn(goalStackPanel, 0, 7)

    }

    fun setTableauPosition(tableauStackPanel: CardStackPanel, row: Int, column: Int) {
        val rowOffset = STACK_LEFT_MARGIN + (ImageManager.getCardWidth() + STACK_HORIZONTAL_SPACING) +
                (ImageManager.getCardWidth() + STACK_HORIZONTAL_SPACING) * (3 - row/2)

        var x = rowOffset + (STACK_HORIZONTAL_SPACING + ImageManager.getCardWidth()) * column
        val y = STACK_TOP_MARGIN + (ImageManager.getCardHeight() + STACK_VERTICAL_SPACING) * row/2
        if (row % 2 == 1) {
            x -= (ImageManager.getCardWidth() + STACK_HORIZONTAL_SPACING)/2
        }
        tableauStackPanel.setLocation(x, y)
    }
}