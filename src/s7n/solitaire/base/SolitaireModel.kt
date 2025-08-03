package s7n.solitaire.base

const val STACK_NAME_DECK = "deck"
const val STACK_NAME_WASTE = "waste"
const val STACKS_NAME_TABLEAUS = "tableaus"
const val STACKS_NAME_GOALS = "goals"
const val STACK_NAME_DRAG = "drag"
const val STACK_NAME_ANIMATION = "animation"
const val STACK_NAME_CANDIDATE = "candidate"

abstract class SolitaireModel {
    private val observers = ArrayList<ChangeObserver>()
    var cheatCount = 0
    var dealCount = 1

    val dragStack = CardStack(STACK_NAME_DRAG, CardStackDragSourceType.None)

    fun addObserver(observer: ChangeObserver) {
        observers.add(observer)
    }

    fun onChange() {
        observers.forEach { it.onChange() }
    }

    protected open fun allGoalsFull(): Boolean {
        val goals = getCardStacks(STACKS_NAME_GOALS)
        var fullGoalCount = 0
        goals.forEach {
            if (it.size == Rank.entries.size)
                fullGoalCount++
        }

        return fullGoalCount == goals.size
    }

    protected fun getCheatCountMesage(): String {
        return if (cheatCount == 1) "once"
        else if (cheatCount == 2) "twice"
        else "$cheatCount times"
    }

    open fun gameIsWon() = allGoalsFull()
    open fun checkCardCount() {}

    abstract fun newGame()
    abstract fun getCardStack(name: String): CardStack
    abstract fun getCardStacks(name: String): List<CardStack>
    abstract fun getStatus(): String
    abstract fun getVictoryMessage(): String
}