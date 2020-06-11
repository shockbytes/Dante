package at.shockbytes.dante.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    BOOK_SUGGESTIONS("book_suggestions", "Suggestions", false);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(BOOK_SUGGESTIONS)
        }
    }
}