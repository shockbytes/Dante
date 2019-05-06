package at.shockbytes.dante.util.settings.delegate

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferencesLongPropertyDelegate(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Long = 0
) : ReadWriteProperty<Any, Long> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.getLong(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }
}