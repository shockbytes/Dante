package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import at.shockbytes.dante.R

object MailLauncher {

    private const val SHOCKBYTES_STUDIO_MAIL = "shockbytesstudio@gmail.com"

    fun sendMail(context: Context, subject: String) {

        val intent = Intent(ACTION_SENDTO)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_EMAIL, SHOCKBYTES_STUDIO_MAIL)
            .putExtra(Intent.EXTRA_SUBJECT, subject)

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_send_mail)))
    }
}