package at.shockbytes.dante.suggestions

data class SuggestionRequest(
    val suggestion: BookSuggestionEntity,
    val recommendation: String
)