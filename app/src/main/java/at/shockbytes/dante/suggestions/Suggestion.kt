package at.shockbytes.dante.suggestions

data class Suggestion(
    val suggestionId: String,
    val suggestion: BookSuggestionEntity,
    val suggester: Suggester,
    val recommendation: String
)
