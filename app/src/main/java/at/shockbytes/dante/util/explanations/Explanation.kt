package at.shockbytes.dante.util.explanations

sealed class Explanation {

    abstract val show: Boolean

    data class Suggestion(
        override val show: Boolean
    ) : Explanation()

    data class Wishlist(
        override val show: Boolean
    ) : Explanation()
}