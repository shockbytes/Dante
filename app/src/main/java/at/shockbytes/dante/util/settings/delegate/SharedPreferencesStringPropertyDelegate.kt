package at.shockbytes.dante.util.settings.delegate

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferencesStringPropertyDelegate(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String = ""
) : ReadWriteProperty<Any, String> {

    override fun getValue(thisRef: Any, property: KProperty<*>): String {
        return preferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        preferences.edit().putString(key, value).apply()
    }
}