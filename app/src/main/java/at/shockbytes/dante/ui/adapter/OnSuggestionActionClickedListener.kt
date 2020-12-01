package at.shockbytes.dante.ui.adapter

import at.shockbytes.dante.suggestions.Suggestion

interface OnSuggestionActionClickedListener {

    fun onAddSuggestionToWishlist(suggestion: Suggestion)

    fun onReportBookSuggestion(suggestionId: String)
}