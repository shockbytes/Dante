package at.shockbytes.dante.util.settings

import androidx.annotation.DrawableRes
import at.shockbytes.dante.R

enum class LauncherIconState(
    private val id: String,
    val manifestAliasId: String,
    val title: String,
    @DrawableRes val icon: Int
) {
    STANDARD(
        id = "standard",
        manifestAliasId = "at.shockbytes.dante.entry.standard",
        title = "Standard",
        icon = R.mipmap.ic_launcher
    ),
    DARK(
        id = "dark",
        manifestAliasId = "at.shockbytes.dante.entry.dark",
        title = "Dark",
        icon = R.mipmap.ic_launcher_dark
    ),
    CLASSIC(
        id = "classic",
        manifestAliasId = "at.shockbytes.dante.entry.classic",
        title = "Classic",
        icon = R.mipmap.ic_launcher_classic
    );

    companion object {

        fun ofStringOrDefault(idString: String): LauncherIconState {
            return values().find { it.id == idString } ?: STANDARD
        }
    }
}