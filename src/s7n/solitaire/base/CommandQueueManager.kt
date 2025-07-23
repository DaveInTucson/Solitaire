package s7n.solitaire.base

import java.util.*
import javax.swing.SwingUtilities

private data class CommandAndCallback(val command: SolitaireCommand, val onCompleted: (() -> Unit)?)

class CommandQueueManager(private val model: SolitaireModel) {
    private val readyQueue: Queue<CommandAndCallback> = LinkedList()
    private val commandStack = Stack<SolitaireCommand>()

    private var isRunning = false

    fun clear() { readyQueue.clear() }

    fun runCommand(command: SolitaireCommand, onCompleted: (() -> Unit)?) {
        readyQueue.add(CommandAndCallback(command, onCompleted))
        tryRunNext()
    }

    fun undoCommand() {
        if (isRunning || commandStack.isEmpty()) return
        isRunning = true
        //println("isRunning = true (undo)")

        val command = commandStack.pop()
        command.undoCommand(model) {
            isRunning = false
            //println("isRunning = false (undo)")
            SwingUtilities.invokeLater { tryRunNext() }
        }
    }


    private fun tryRunNext() {
        if (isRunning || readyQueue.isEmpty()) return

        isRunning = true
        //println("isRunning = true")
        val commandAndCallback = readyQueue.remove()
        commandAndCallback.command.doCommand(model) { success ->
            if (success) {
                model.onChange()
                commandStack.push(commandAndCallback.command)
            }
            commandAndCallback.onCompleted?.let { it() }
            // Defer next attempt so it doesn't recurse
            isRunning = false
            SwingUtilities.invokeLater { tryRunNext() }
        }
    }
}