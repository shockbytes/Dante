package at.shockbytes.dante.util.settings

import androidx.appcompat.app.AppCompatDelegate

enum class ThemeState(
    private val stringRepresentation: String,
    val themeMode: Int
) {
    LIGHT(
        "light",
        AppCompatDelegate.MODE_NIGHT_NO
    ),
    DARK(
        "dark",
        AppCompatDelegate.MODE_NIGHT_YES
    ),
    SYSTEM(
        "system",
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    );

    companion object {

        fun ofString(str: String): ThemeState? {
            return values().find { it.stringRepresentation == str }
        }

        fun ofStringWithDefault(str: String): ThemeState {
            return values().find { it.stringRepresentation == str } ?: SYSTEM
        }
    }
}