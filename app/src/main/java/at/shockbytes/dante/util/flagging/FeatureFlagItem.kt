package at.shockbytes.dante.util.flagging

data class FeatureFlagItem(
    val key: String,
    val displayName: String,
    val value: Boolean
)