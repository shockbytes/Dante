package at.shockbytes.dante.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {

    INSPIRATIONS("inspirations", "Inspirations", true);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(INSPIRATIONS)
        }
    }
}