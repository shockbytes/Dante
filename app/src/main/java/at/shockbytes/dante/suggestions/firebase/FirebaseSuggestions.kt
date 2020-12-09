package at.shockbytes.dante.suggestions.firebase

class FirebaseSuggestions(
    val suggestions: List<FirebaseSuggestion> // TODO Replace with real data class later
) {

    data class FirebaseSuggestion(
        val title: String
    )
}