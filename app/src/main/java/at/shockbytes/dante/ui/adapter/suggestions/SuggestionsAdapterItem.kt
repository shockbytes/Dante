package at.shockbytes.dante.ui.adapter.suggestions

import at.shockbytes.dante.R
import at.shockbytes.dante.suggestions.Suggestion

sealed class SuggestionsAdapterItem {

    abstract val id: String
    abstract val viewType: Int

    data class SuggestedBook(
        val suggestion: Suggestion,
        override val viewType: Int = R.layout.item_suggestion
    ) : SuggestionsAdapterItem() {

        override val id: String
            get() = suggestion.suggestionId
    }

    data class SuggestionHint(
        override val id: String = SUGGESTION_HINT_ID,
        override val viewType: Int = R.layout.item_generic_explanation
    ) : SuggestionsAdapterItem()

    companion object {

        private const val SUGGESTION_HINT_ID = "-1"
    }
}
