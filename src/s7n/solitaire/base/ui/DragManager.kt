package s7n.solitaire.base.ui

import s7n.solitaire.base.SolitaireCommand
import java.awt.event.MouseEvent

interface DragManager {
    fun install(parent: SolitairePanel)
    fun onPress(e: MouseEvent)
    fun onDrag(e: MouseEvent)
    fun onDragEnd(e: MouseEvent): SolitaireCommand?
}