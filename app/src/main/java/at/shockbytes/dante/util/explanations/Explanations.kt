package at.shockbytes.dante.util.explanations


interface Explanations {

    fun suggestion(): Explanation.Suggestion

    fun wishlist(): Explanation.Wishlist

    fun markSeen(explanation: Explanation)
}
