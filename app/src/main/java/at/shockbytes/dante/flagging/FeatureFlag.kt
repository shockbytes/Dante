package at.shockbytes.dante.flagging

sealed class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    object BookSuggestions : FeatureFlag("book_suggestions", "Book suggestions", false)
    object UpdatedDetailPage : FeatureFlag("detail_page_v2", "Updated detail page", true)
    object ScannerImprovements : FeatureFlag("sanner_v2", "Book scanner v2", true)

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(BookSuggestions, UpdatedDetailPage, ScannerImprovements)
        }
    }
}