package at.shockbytes.dante.suggestions

data class Suggestion(
    val suggestion: BookSuggestionEntity,
    val suggester: Suggester,
    val recommendation: String
)
