package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggester

data class RawSuggestions(
    val suggestions: List<RawSuggestion>
) {

    data class RawSuggestion(
        val suggestionId: String,
        val suggestion: BookSuggestionEntity,
        val suggester: Suggester,
        val recommendation: String,
        val likes: Int
    )
}