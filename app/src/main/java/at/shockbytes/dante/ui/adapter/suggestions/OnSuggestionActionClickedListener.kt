package at.shockbytes.dante.ui.adapter.suggestions

import at.shockbytes.dante.suggestions.Suggestion

interface OnSuggestionActionClickedListener {

    fun onAddSuggestionToWishlist(suggestion: Suggestion)

    fun onReportBookSuggestion(suggestionId: String, suggestionTitle: String)

    fun onLikeBookSuggestion(suggestionId: String, suggestionTitle: String, isLikedByMe: Boolean)
}