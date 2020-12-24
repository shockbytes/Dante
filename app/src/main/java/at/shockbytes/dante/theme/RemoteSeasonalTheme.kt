package at.shockbytes.dante.theme

data class RemoteSeasonalTheme(
    val name: String,
    val type: String,
    val resource: String,
    protected val resource_speed: Float?
) {
    val resourceSpeed: Float
        get() = resource_speed ?: 1f
}
