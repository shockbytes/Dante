package at.shockbytes.dante.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    BOOK_SUGGESTIONS("book_suggestions", "Suggestions", false),
    OVERFLOW_MENU("remove_overflow_menu", "Overflow v2", false);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(BOOK_SUGGESTIONS, OVERFLOW_MENU)
        }
    }
}