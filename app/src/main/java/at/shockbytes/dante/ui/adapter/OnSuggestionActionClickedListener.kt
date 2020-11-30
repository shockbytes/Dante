package at.shockbytes.dante.ui.adapter

import at.shockbytes.dante.suggestions.BookSuggestionEntity

interface OnSuggestionActionClickedListener {

    fun onAddSuggestionToWishlist(data: BookSuggestionEntity)

    fun onReportBookSuggestion(suggestionId: String)
}