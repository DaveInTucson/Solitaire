package s7n.solitaire.base.ui

import s7n.solitaire.base.Card
import s7n.solitaire.base.Rank
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.swing.ImageIcon

private fun getImage(path: String): ImageIcon {
    return  ImageIcon(Card::class.java.getResource(path))
}

private fun getRankDesignator(rank: Rank): String {
    return when(rank) {
        Rank.Ace -> "A"
        Rank.Two -> "2"
        Rank.Three -> "3"
        Rank.Four -> "4"
        Rank.Five -> "5"
        Rank.Six -> "6"
        Rank.Seven -> "7"
        Rank.Eight -> "8"
        Rank.Nine -> "9"
        Rank.Ten -> "10"
        Rank.Jack -> "J"
        Rank.Queen -> "Q"
        Rank.King -> "K"
    }
}

private fun getCardImagePath(card: Card): String {
    return "/images/cards/card${card.suit}${getRankDesignator(card.rank)}.png"
}

private const val CARD_BACK_PATH = "/images/cards/cardBack_blue5.png"

private const val DISPLAY_CARD_WIDTH = 70
private const val DISPLAY_CARD_HEIGHT = 85

object ImageManager {
    private val imageCache = mutableMapOf<String, Image>()

    private val cardBackImage = getImage("/images/cards/cardBack_blue5.png")

    private fun getCardImage(card: Card): Image {
        val path = getCardImagePath(card)
        val cachedImage = imageCache[path]
        if (cachedImage != null) return cachedImage

        val loadedImage = getImage(path).image
        imageCache[path] = loadedImage
        return loadedImage
    }

    fun getCardDimension(): Dimension {
        return Dimension(getCardWidth(), getCardHeight())
    }

    fun getCardWidth() = DISPLAY_CARD_WIDTH

    fun getCardHeight() = DISPLAY_CARD_HEIGHT

    fun drawRect(g: Graphics) {
        g.color = Color.WHITE
        g.drawRoundRect(0, 0, getCardWidth()-1, getCardHeight()-1, 5, 5)
    }

    fun drawCard(g: Graphics, card: Card, xPos: Int, yPos: Int, cardStackPanel: CardStackPanel) {
        val cardImage = if (card.faceUp) getCardImage(card) else cardBackImage.image
        g.drawImage(cardImage, xPos, yPos, getCardWidth(), getCardHeight(), cardStackPanel)
    }
}