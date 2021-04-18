package at.shockbytes.dante.core.flagging

data class FeatureFlagItem(
    val key: String,
    val displayName: String,
    val value: Boolean
)