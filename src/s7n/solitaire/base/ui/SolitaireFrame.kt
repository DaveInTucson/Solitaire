package s7n.solitaire.base.ui

import s7n.solitaire.canfield.CanfieldPanel
import s7n.solitaire.freeCell.FreeCellPanel
import s7n.solitaire.klondike.KlondikePanel
import s7n.solitaire.pyramid.PyramidPanel
import s7n.solitaire.spider.SpiderPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.ButtonGroup
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JRadioButtonMenuItem
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

enum class GameNames {
    Klondike, Canfield, FreeCell, Spider, Pyramid
}

class SolitaireFrame: JFrame("Dave's Solitaire") {
    private val statusPanel = StatusPanel()

    private val gamePanels = arrayOf(
        KlondikePanel(GameNames.Klondike, statusPanel),
        CanfieldPanel(GameNames.Canfield, statusPanel),
        FreeCellPanel(GameNames.FreeCell, statusPanel),
        SpiderPanel(GameNames.Spider, statusPanel),
        PyramidPanel(GameNames.Pyramid, statusPanel)
    )

    private var currentGameIndex = 0

    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)

    init  {
        defaultCloseOperation = EXIT_ON_CLOSE

        jMenuBar = createMenuBar()

        gamePanels.forEach {
            cardPanel.add(it, it.gameName.toString())
        }
        add(cardPanel)

        statusPanel.preferredSize = Dimension(preferredSize.width, 16)
        add(statusPanel, BorderLayout.SOUTH)

        // pack()
        showCurrentGame()
        setLocationRelativeTo(null)
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()
        menuBar.add(createFileMenu())
        menuBar.add(createGameMenu())
        return menuBar
    }

    private fun createFileMenu() : JMenu {
        val menu = JMenu("File")
        val newGame = JMenuItem("New Game")
        newGame.addActionListener {
            gamePanels[currentGameIndex].onNewGame()
        }

        menu.add(newGame)

        val undo = JMenuItem("Undo Move")
        undo.addActionListener {
            gamePanels[currentGameIndex].onUndoMove()
        }

        menu.add(undo)

        val cheat = JMenuItem("Cheat")
        cheat.addActionListener {
            gamePanels[currentGameIndex].onCheat()
        }

        menu.add(cheat)

        val exit = JMenuItem("Exit")
        exit.addActionListener {
            exitProcess(0)
        }
        menu.add(exit)

        return menu
    }

    private fun createGameMenu(): JMenu {
        val menu = JMenu("Select Game")

        val group = ButtonGroup()

        for (i in gamePanels.indices) {
            val button = JRadioButtonMenuItem(gamePanels[i].gameName.toString())
            button.isSelected = i == currentGameIndex
            button.addActionListener { onSelectGame(i) }
            group.add(button)
            menu.add(button)
        }


        return menu
    }

    private fun onSelectGame(selectedIndex: Int) {
        if (currentGameIndex == selectedIndex) return
        currentGameIndex = selectedIndex
        showCurrentGame()
    }

    private fun showCurrentGame() {
        val gamePanel = gamePanels[currentGameIndex]
        title = "Dave's Solitaire - ${gamePanel.gameName}"
        cardLayout.show(cardPanel, gamePanel.gameName.toString())
        cardPanel.preferredSize = gamePanel.preferredSize
        preferredSize = gamePanel.preferredSize
        gamePanel.onVisible()
        pack()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { SolitaireFrame().isVisible = true }
        }
    }
}