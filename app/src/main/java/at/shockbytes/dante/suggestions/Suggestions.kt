package at.shockbytes.dante.suggestions

data class Suggestions(val suggestions: List<Suggestion>) {

    fun isNotEmpty(): Boolean {
        return suggestions.isNotEmpty()
    }
}
