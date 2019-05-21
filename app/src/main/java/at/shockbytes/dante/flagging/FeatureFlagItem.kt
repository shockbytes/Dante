package at.shockbytes.dante.flagging

data class FeatureFlagItem(
    val key: String,
    val displayName: String,
    val value: Boolean
)