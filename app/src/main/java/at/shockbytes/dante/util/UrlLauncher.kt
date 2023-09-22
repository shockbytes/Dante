package at.shockbytes.dante.util

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

object UrlLauncher {

    private const val toolbarColor = "#2196f3"

    private const val DANTE_GITHUB_URL = "https://github.com/shockbytes/Dante"
    private const val DISCORD_PAGE_URL = "https://discord.gg/EujYrCHjkm"
    private const val TOS_URL = "https://dantebooks.com/#/terms"
    private const val PRIVACY_URL = "https://dantebooks.com/#/privacy"

    fun openDanteGithubPage(context: Context) {
        launchUrl(context, DANTE_GITHUB_URL)
    }

    fun openDiscordPage(context: Context) {
        launchUrl(context, DISCORD_PAGE_URL)
    }
    
    fun openTermsOfServicePage(context: Context) {
        launchUrl(context, TOS_URL)
    }

    fun openPrivacyPolicy(context: Context) {
        launchUrl(context, PRIVACY_URL)
    }

    fun launchUrl(context: Context, url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
                .setToolbarColor(Color.parseColor(toolbarColor))
                .build()
            )
            .build()

        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}