package at.shockbytes.dante.flagging

sealed class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    object BookSuggestions : FeatureFlag("book_suggestions", "Book suggestions", false)
    object OverflowMenu : FeatureFlag("remove_overflow_menu", "Replace Overflow menu", false)

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(BookSuggestions, OverflowMenu)
        }
    }
}