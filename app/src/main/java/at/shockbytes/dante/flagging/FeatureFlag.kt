package at.shockbytes.dante.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {
    Unused("", "", false);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf()
        }
    }
}