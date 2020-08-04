package at.shockbytes.dante.util

import android.app.Activity
import androidx.core.app.ShareCompat
import at.shockbytes.dante.R

object MailLauncher {

    private const val SHOCKBYTES_STUDIO_MAIL = "shockbytesstudio@gmail.com"

    fun sendMail(
        activity: Activity,
        subject: String,
        body: String = "",
        recipient: String = SHOCKBYTES_STUDIO_MAIL
    ) {

        ShareCompat.IntentBuilder.from(activity)
            .setType("message/rfc822")
            .addEmailTo(recipient)
            .setSubject(subject)
            .setText(body)
            .setChooserTitle(activity.getString(R.string.action_send_mail))
            .startChooser()
    }
}