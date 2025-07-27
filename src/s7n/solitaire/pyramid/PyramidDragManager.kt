package s7n.solitaire.pyramid

import s7n.solitaire.base.SolitaireCommand
import s7n.solitaire.base.ui.CardStackPanel
import s7n.solitaire.base.ui.KlondikeDragManager

class PyramidDragManager(dragStackPanel: CardStackPanel, private val goalPanel: CardStackPanel) : KlondikeDragManager(dragStackPanel) {

    override fun getDragCommand(
        sourcePanel: CardStackPanel,
        targetPanel: CardStackPanel,
        dragPanel: CardStackPanel
    ): SolitaireCommand {
        return PyramidDragCommand(sourcePanel, targetPanel, dragPanel, goalPanel)
    }
}