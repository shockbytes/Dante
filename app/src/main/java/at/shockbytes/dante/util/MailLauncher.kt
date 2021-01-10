package at.shockbytes.dante.util

import android.app.Activity
import androidx.core.app.ShareCompat
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.R

object MailLauncher {

    private const val SHOCKBYTES_STUDIO_MAIL = "shockbytesstudio@gmail.com"

    fun sendMail(
        activity: Activity,
        subject: String,
        body: String = "",
        recipient: String = SHOCKBYTES_STUDIO_MAIL,
        attachVersion: Boolean = false
    ) {

        val formattedBody = if (attachVersion) {
            "$body\n\n\n${getVersionDetails()}"
        } else body

        ShareCompat.IntentBuilder.from(activity)
            .setType("message/rfc822")
            .addEmailTo(recipient)
            .setSubject(subject)
            .setText(body)
            .setChooserTitle(activity.getString(R.string.action_send_mail))
            .startChooser()
    }

    private fun getVersionDetails(): String {
        return "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }
}