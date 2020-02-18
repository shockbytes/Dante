package at.shockbytes.dante.util.settings

enum class LauncherIconState(
    private val id: String,
    val manifestAliasId: String
) {
    STANDARD("standard", "at.shockbytes.dante.entry.standard"),
    DARK("dark", "at.shockbytes.dante.entry.dark"),
    CLASSIC("classic", "at.shockbytes.dante.entry.classic");

    companion object {

        fun ofStringOrDefault(idString: String): LauncherIconState {
            return values().find { it.id == idString } ?: STANDARD
        }
    }
}