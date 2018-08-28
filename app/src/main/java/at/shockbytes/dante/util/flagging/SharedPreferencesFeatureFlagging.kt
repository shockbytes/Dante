package at.shockbytes.dante.util.flagging

import android.content.SharedPreferences

/**
 * @author  Martin Macheiner
 * Date:    28.08.2018
 */
class SharedPreferencesFeatureFlagging(private val preferences: SharedPreferences): FeatureFlagging {

    override val showSupportersBadge: Boolean
        get() = preferences.getBoolean("supporters_badge_flag", false)

    override val showBookSuggestions: Boolean
        get() = preferences.getBoolean("show_suggestions_flag", false)
}