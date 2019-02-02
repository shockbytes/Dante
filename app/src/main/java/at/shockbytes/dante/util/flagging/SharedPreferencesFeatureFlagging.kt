package at.shockbytes.dante.util.flagging

import android.content.SharedPreferences

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
class SharedPreferencesFeatureFlagging(private val preferences: SharedPreferences) : FeatureFlagging {

    override fun get(flag: FeatureFlag): Boolean {
        return preferences.getBoolean(flag.key, flag.defaultValue)
    }

    override fun updateFlag(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }
}