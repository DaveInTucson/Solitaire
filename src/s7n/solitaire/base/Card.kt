package s7n.solitaire.base

enum class Suit {
    Spades, Hearts, Clubs, Diamonds;

    val letter
        get(): String =
            when (this) {
                Spades   -> "s"
                Hearts   -> "h"
                Clubs    -> "c"
                Diamonds -> "d"
            }

    val color
        get(): String =
            when (this) {
                Spades   -> "black"
                Hearts   -> "red"
                Clubs    -> "black"
                Diamonds -> "red"
            }

    val symbol
        get(): String =
            when (this) {
                Spades   -> "♠"
                Hearts   -> "♥"
                Clubs    -> "♣"
                Diamonds -> "♦"
            }
}

enum class Rank(val numericRank: Int) {
    Ace(1), Two(2), Three(3), Four(4), Five(5), Six(6),
    Seven(7), Eight(8), Nine(9), Ten(10), Jack(11),
    Queen(12), King(13);

    val shortName
        get(): String =
            when (this) {
                Ace -> "A"
                Two -> "2"
                Three -> "3"
                Four -> "4"
                Five -> "5"
                Six -> "6"
                Seven -> "7"
                Eight -> "8"
                Nine -> "9"
                Ten -> "10"
                Jack -> "J"
                Queen -> "Q"
                King -> "K"
            }

    val next
        get(): Rank =
            when (this) {
                Ace -> Two
                Two -> Three
                Three -> Four
                Four -> Five
                Five -> Six
                Six -> Seven
                Seven -> Eight
                Eight -> Nine
                Nine -> Ten
                Ten -> Jack
                Jack -> Queen
                Queen -> King
                King -> Ace
            }

    val prev
        get(): Rank =
            when(this) {
                Ace -> King
                Two -> Ace
                Three -> Two
                Four -> Three
                Five -> Four
                Six -> Five
                Seven -> Six
                Eight -> Seven
                Nine -> Eight
                Ten -> Nine
                Jack -> Ten
                Queen -> Jack
                King -> Queen
            }
}

class Card(val rank: Rank, val suit: Suit) {

    var faceUp = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false

        return suit == other.suit && rank == other.rank
    }

    override fun hashCode(): Int {
        return 31 * suit.hashCode() + rank.hashCode()
    }

    override fun toString(): String {
        val dir = if (faceUp) "^" else "v"
        return  "${rank.shortName} of ${suit.letter}($dir)"
    }
}
