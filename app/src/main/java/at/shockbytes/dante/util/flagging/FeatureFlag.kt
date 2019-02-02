package at.shockbytes.dante.util.flagging

sealed class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    object BookSuggestions : FeatureFlag("book_suggestions", "Book suggestions", false)
    object UpdatedDetailPage : FeatureFlag("detail_page_v2", "Updated detail page", true)
}