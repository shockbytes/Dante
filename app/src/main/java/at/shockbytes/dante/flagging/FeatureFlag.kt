package at.shockbytes.dante.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    BOOK_SUGGESTIONS("book_suggestions", "Suggestions", false),
    PAGE_RECORD_STATISTICS("page_record_statistics", "Page records stats", false);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(BOOK_SUGGESTIONS, PAGE_RECORD_STATISTICS)
        }
    }
}