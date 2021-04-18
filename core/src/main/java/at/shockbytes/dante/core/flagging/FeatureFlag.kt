package at.shockbytes.dante.core.flagging

enum class FeatureFlag(val key: String, val displayName: String, val defaultValue: Boolean) {
    FireFlash("key-fireflash", "Fireflash \uD83D\uDD25", defaultValue = false),
    Unused("", "", false);

    companion object {

        fun activeFlags(): List<FeatureFlag> {
            return listOf(FireFlash)
        }
    }
}