package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlLauncher {

    private const val DANTE_GITHUB_URL = "https://github.com/shockbytes/Dante"
    private const val AD_FREE_MEDIUM_URL = "https://mescht93.medium.com/why-my-apps-remain-ad-free-26d29217bdec"
    private const val TOS_URL = "https://dantebooks.app/terms"

    fun openAdFreeMediumArticle(context: Context) {
        launchUrl(context, AD_FREE_MEDIUM_URL)
    }

    fun openDanteGithubPage(context: Context) {
        launchUrl(context, DANTE_GITHUB_URL)
    }
    
    fun openTermsOfServicePage(context: Context) {
        launchUrl(context, TOS_URL)
    }

    fun launchUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
        context.startActivity(intent)
    }
}