package s7n.solitaire.base.ui

import s7n.solitaire.base.*
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JOptionPane
import javax.swing.JPanel

const val FELT_GREEN_RGB = 0x277714

const val PANEL_HEIGHT = 500

const val STACK_TOP_MARGIN = 20
const val STACK_LEFT_MARGIN = 20
const val STACK_HORIZONTAL_SPACING = 20
const val STACK_VERTICAL_SPACING = 20

fun setRowColumn(panel: JPanel, row: Int, col: Int) {
    val x = STACK_LEFT_MARGIN + (ImageManager.getCardWidth() + STACK_HORIZONTAL_SPACING) * col
    val y = STACK_TOP_MARGIN + (ImageManager.getCardHeight() + STACK_VERTICAL_SPACING) * row
    panel.setLocation(x, y)
}

fun computePreferredWidth(columnCount: Int): Int {
    return STACK_LEFT_MARGIN * 2 + (ImageManager.getCardWidth() + STACK_HORIZONTAL_SPACING) * columnCount
}

abstract class SolitairePanel(val gameName: GameNames, private val statusPanel: StatusPanel): JPanel() {
    private var firstTimeVisible = true

    protected val dragStackPanel : CardStackPanel by lazy { CardStackPanel(getModel().dragStack, true) }
    protected val animationPanel = CardStackPanel(CardStack(STACK_NAME_ANIMATION, CardStackDragSourceType.None), true)

    private val commandStackManager by lazy { CommandQueueManager(getModel()) }

    protected abstract fun getDragManager(): DragManager
    protected abstract fun getModel(): SolitaireModel

    protected fun initialize() {
        background = Color(FELT_GREEN_RGB)
        layout = null

        getDragManager().install(this)
        animationPanel.setHidden()
        add(animationPanel)
    }

    fun onVisible() {
        if (firstTimeVisible) {
            firstTimeVisible = false
            getModel().addObserver {
                updateStatus()
                checkForWin()
            }
        }
        updateStatus()
    }

    private fun updateStatus()
    { statusPanel.setStatus(getModel().getStatus()) }

    fun addListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (e != null) getDragManager().onPress(e)
            }

            override fun mouseReleased(e: MouseEvent?) {
                if (e != null) {
                    val command = getDragManager().onDragEnd(e)
                    doCommandAndPost(command) {
                        //println("clearing drag stack")
                        dragStackPanel.cardStack.clear()
                        dragStackPanel.setHidden()
                    }
                }
            }

            override fun mouseClicked(e: MouseEvent?) {
                if (e != null) onClick(e)
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (e != null) getDragManager().onDrag(e)
            }
        })
    }

    fun onClick(e: MouseEvent) {
        when (val component = e.component.getComponentAt(e.point)) {
            is CardStackPanel -> {
                val command = component.onClick(e.clickCount)
                println("onClick count=${e.clickCount} command=$command")
                doCommandAndPost(command) {
                    //println("clickCount=${e.clickCount}")
                    if (e.clickCount > 1) autoPromoteToGoal()
                }
            }
        }

    }

    protected fun doCommandAndPost(command: SolitaireCommand?, onComplete: (() -> Unit)? = null) {
        doCommand(command) {
            onComplete?.let { it() }
            doPostCommand()
        }
    }

    // This is for any post-command processing that needs to be done. In Canfield, when a command
    // causes a tableau stack to be empty, this is used to promote the top reserve card (if any)
    // to that empty spot
    protected open fun doPostCommand() {
        // empty body
    }

    protected fun doCommand(command: SolitaireCommand?, onCompleted: ((Boolean) -> Unit)?)  {
        if (command == null) {
            onCompleted?.let { it(false) }
            return
        }
        commandStackManager.runCommand(command, onCompleted)
    }

    fun onUndoMove() {
        commandStackManager.undoCommand()
        updateStatus()
    }

    fun onNewGame() {
        commandStackManager.clear()
        getModel().newGame()
    }

    abstract fun onCheat()
    abstract fun autoPromoteToGoal()

    open fun checkForWin() {
        if (getModel().gameIsWon()) {
            commandStackManager.clear()
            val victoryMessage = getModel().getVictoryMessage()
            val anotherGame = JOptionPane.showConfirmDialog(
                this, "$victoryMessage Another game?",
                "You win!",
                JOptionPane.YES_NO_OPTION)
            if (anotherGame == JOptionPane.YES_OPTION)
                onNewGame()
        }
    }
}