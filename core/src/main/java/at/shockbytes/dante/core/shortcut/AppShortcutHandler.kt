package at.shockbytes.dante.core.shortcut

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.shockbytes.dante.core.R
import at.shockbytes.dante.core.login.LoginRepository
import javax.inject.Inject

class AppShortcutHandler @Inject constructor(private val loginRepository: LoginRepository) {

    fun handleAppShortcutForActivity(
        activity: AppCompatActivity,
        shortcutTitle: String,
        action: () -> Unit,
    ) {

        if (activity.intent.hasExtra(INTENT_EXTRA_APP_SHORTCUT)) {

            if (loginRepository.isLoggedIn()) {
                if (activity.intent.getStringExtra(INTENT_EXTRA_APP_SHORTCUT) == shortcutTitle) {
                    action()

                    // Consume extra in order to not be reused, but only if action matches
                    val defaultValue: String? = null
                    activity.intent.putExtra(INTENT_EXTRA_APP_SHORTCUT, defaultValue)
                }
            } else {
                Toast.makeText(activity, R.string.unauthenticated_shortcut_usage, Toast.LENGTH_LONG).show()
                activity.supportFinishAfterTransition()
            }
        }
    }

    companion object {

        private const val INTENT_EXTRA_APP_SHORTCUT = "app_shortcut"
    }
}