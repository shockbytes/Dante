package at.shockbytes.dante.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.R

/**
 * @author Martin Macheiner
 * Date: 11.02.2018.
 */

class DanteSettings(private val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pageTrackingEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_page_tracking_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_page_tracking_key), value)
                    .apply()
        }

    var pageOverlayEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_page_overlay_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_page_overlay_key), value)
                    .apply()
        }

}