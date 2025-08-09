package s7n.solitaire.base

import java.util.*
import javax.swing.SwingUtilities

private data class CommandAndCallback(val command: SolitaireCommand, val onCompleted: ((Boolean) -> Unit)?)

class CommandQueueManager(private val model: SolitaireModel) {
    private val readyQueue: Queue<CommandAndCallback> = LinkedList()
    private val executedCommandStack = Stack<SolitaireCommand>()

    private var isRunning = false

    fun clear() {
        readyQueue.clear()
    }

    fun runCommand(command: SolitaireCommand, onCompleted: ((Boolean) -> Unit)?) {
        readyQueue.add(CommandAndCallback(command, onCompleted))
        tryRunNext()
    }

    fun undoCommand() {
        if (isRunning || executedCommandStack.isEmpty()) return
        isRunning = true

        val command = executedCommandStack.pop()
        command.undoCommand(model) {
            isRunning = false
            SwingUtilities.invokeLater { tryRunNext() }
        }
    }


    private fun tryRunNext() {
        if (isRunning || readyQueue.isEmpty()) return

        isRunning = true
        val commandAndCallback = readyQueue.remove()
        commandAndCallback.command.doCommand(model) { success ->
            if (success) {
                model.onChange()
                executedCommandStack.push(commandAndCallback.command)
            }
            commandAndCallback.onCompleted?.let { it(success) }
            // Defer next attempt so it doesn't recurse
            isRunning = false
            SwingUtilities.invokeLater { tryRunNext() }
        }
    }
}