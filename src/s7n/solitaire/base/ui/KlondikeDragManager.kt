package s7n.solitaire.base.ui

import s7n.solitaire.base.CardStack
import s7n.solitaire.base.DragCardsCommand
import s7n.solitaire.base.STACK_NAME_DRAG
import s7n.solitaire.base.SolitaireCommand
import s7n.solitaire.base.ui.*
import java.awt.Component
import java.awt.Container
import java.awt.Point
import java.awt.event.MouseEvent

/**
 *  The drag manager class takes the various relevant mouse events (mouse click, mouse
 * drag, mouse release) and turns them into cards being dragged around the playing field.
 * There is a special CardStackPanel used for this purpose (normally hidden from view).
 * This class manages getting cards to drag from the source panel, moving the drag panel
 * around on the screen, and checking to see if the drag panel was released on a stack that
 * is willing to accept the dragged card(s).
 *
 * The end result of a successful card drag is a DragCardsCommand object returned by
 * the onDragEnd method, which is then used to complete the drag operation.
 *
 * This drag manager was initially implemented for Klondike (the first game I did), and it
 * turns out to work just fine for Canfield, Free Cell, and Spider.
 *
 * By default, a {@link DragCardsCommand} is issued, which simply adds the dragged card(s) to the
 * target stack. For games like Pyramid (where the dragged card and the target card are
 * moved to the goal) you will have to override {@link #getDragCommand()} to implement the
 * behavior that you want.
 */
open class KlondikeDragManager(private val dragStackPanel: CardStackPanel): DragManager {
    private val crosshairPanel = CrosshairPanel()

    val isDragging: Boolean
        get() = dragSourcePanel != null

    private var dragPoint = Point(0, 0)
    private var dragSourcePanel: CardStackPanel? = null

    override fun install(parent: SolitairePanel) {
        dragStackPanel.setHidden()
        parent.add(dragStackPanel)

        // crosshairPanel gives a visual representation of where the dropPoint is whenever
        // there is a failed drag (see onDragEnd below). To enable it, uncomment the line
        // that adds it to the parent panel.
        crosshairPanel.setHidden()
        //parent.add(crosshairPanel) // uncomment this line to enable crosshair feature
    }

    override fun onPress(e: MouseEvent) {
        dragPoint = e.point
    }

    override fun onDrag(e: MouseEvent) {
        if (!isDragging) {
            val candidateDragSource = e.component.getComponentAt(e.point)
            if (candidateDragSource is CardStackPanel) {
                val candidateStack = candidateDragSource.getDragStack(e.point)
                if (candidateStack.isEmpty()) return
                dragPoint = e.point
                dragSourcePanel = candidateDragSource
                assert(dragStackPanel.cardStack.isEmpty()) { "dragStack=${dragStackPanel.cardStack}"}
                dragStackPanel.cardStack.add(candidateStack)
                candidateDragSource.setDragPanelPosition(dragStackPanel, e.point)
                candidateDragSource.repaint()
            }
        }

        dragTo(e.point)
    }

    private fun dragTo(point: Point) {
        if (isDragging) {
            val delX = point.x - dragPoint.x
            val delY = point.y - dragPoint.y
            val l = dragStackPanel.location
            l.translate(delX, delY)
            dragStackPanel.location = l
            dragPoint = point
            dragStackPanel.repaint()
        }
    }

    /**  Determines the drop point, the point to check where the drag is released
     * (and specifically what's underneath it).
     */
    private fun getDropPoint(): Point {
        val x = dragStackPanel.location.x + dragStackPanel.width/2
        val y = dragStackPanel.location.y + ImageManager.getCardHeight()/2
        return Point(x, y)
    }

    private fun isDropTarget(component: Component, dropPoint: Point, dropStack: CardStack): Boolean {
        return component.bounds.contains(dropPoint) &&
                component is CardStackPanel &&
                component.cardStack.accepts(dropStack)
    }

    private fun getDropTarget(component: Component, dropPoint: Point, dropStack: CardStack): CardStackPanel? {
        if (component is Container) {
            return component.components.find {
                isDropTarget(it, dropPoint, dropStack)
            } as CardStackPanel?
        }

        return null
    }

    override fun onDragEnd(e: MouseEvent): SolitaireCommand? {
        var result: SolitaireCommand? = null

        dragSourcePanel?.let {
            val dropTarget = getDropTarget(e.component, getDropPoint(), dragStackPanel.cardStack)
            require (dropTarget== null || dropTarget.cardStack.accepts(dragStackPanel.cardStack)) {
                "getDropTarget returned illegal candidate"
            }

            if (dropTarget != null) {
                result = getDragCommand(it, dropTarget, dragStackPanel)
            }
            else {
                // return dragged cards to their source
                it.cardStack.add(dragStackPanel.cardStack)

                // feedback for debugging failed drop. (crosshairPanel must be added in #install above)
                crosshairPanel.setPosition(getDropPoint())
//                if (dropTarget is CardStackPanel) {
//                    println("dropTarget doesn't accept card stack")
//                }
//                else
//                    println("dropTarget is not a CardStackPanel")
            }

            dragSourcePanel = null
        }

        return result
    }

    protected open fun getDragCommand(
        sourcePanel: CardStackPanel,
        targetPanel: CardStackPanel,
        dragPanel: CardStackPanel) : SolitaireCommand {
        return DragCardsCommand(sourcePanel, targetPanel, dragStackPanel)
    }
}