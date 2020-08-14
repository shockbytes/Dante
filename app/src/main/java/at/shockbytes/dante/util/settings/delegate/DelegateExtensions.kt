package at.shockbytes.dante.util.settings.delegate

import android.content.SharedPreferences


fun SharedPreferences.boolDelegate(
        key: String,
        defaultValue: Boolean = true
): SharedPreferencesBoolPropertyDelegate = SharedPreferencesBoolPropertyDelegate(this, key, defaultValue)

fun SharedPreferences.stringDelegate(
        key: String,
        defaultValue: String
): SharedPreferencesStringPropertyDelegate = SharedPreferencesStringPropertyDelegate(this, key, defaultValue)