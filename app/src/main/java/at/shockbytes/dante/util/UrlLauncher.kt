package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlLauncher {

    private const val DANTE_GITHUB_URL = "https://github.com/shockbytes/Dante"

    fun openDanteGithubPage(context: Context) {
        launchUrl(context, DANTE_GITHUB_URL)
    }

    fun launchUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
        context.startActivity(intent)
    }
}