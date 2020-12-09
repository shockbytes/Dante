package at.shockbytes.dante.suggestions

data class SuggestionRequest(
    val bookSuggestionEntity: BookSuggestionEntity,
    val recommendation: String
)