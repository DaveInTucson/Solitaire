package s7n.solitaire.base.ui

import s7n.solitaire.base.SolitaireCommand
import java.awt.event.MouseEvent

interface DragManager {
    abstract fun install(parent: SolitairePanel)
    abstract fun onPress(e: MouseEvent)
    abstract fun onDrag(e: MouseEvent)
    abstract fun onDragEnd(e: MouseEvent): SolitaireCommand?
}