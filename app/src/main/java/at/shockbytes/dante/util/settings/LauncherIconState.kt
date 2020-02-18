package at.shockbytes.dante.util.settings

enum class LauncherIconState(
    private val stringRepresentation: String
) {
    STANDARD("standard"),
    DARK("dark"),
    CLASSIC("classic");

    companion object {

        fun ofStringOrDefault(str: String): LauncherIconState {
            return values().find { it.stringRepresentation == str } ?: STANDARD
        }
    }
}