package at.shockbytes.dante.backup.provider.shockbytes.storage

import android.content.SharedPreferences
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.util.fromJson
import com.google.gson.Gson

class SharedPreferencesInactiveShockbytesBackupStorage(
    private val sharedPreferences: SharedPreferences
) : InactiveShockbytesBackupStorage {

    private val gson: Gson = Gson()

    override fun getInactiveItems(): List<BackupEntryState> {
        return sharedPreferences.getString(KEY_INACTIVE_ITEMS, null)?.let { jsonEncoded ->
            gson.fromJson<List<BackupEntry>>(jsonEncoded)
                .map { BackupEntryState.Inactive(it) }
        } ?: listOf()
    }

    override fun storeInactiveItems(items: List<BackupEntryState>) {

        val json = gson.toJson(items.map { it.entry })
        sharedPreferences.edit().putString(KEY_INACTIVE_ITEMS, json).apply()
    }

    companion object {

        private const val KEY_INACTIVE_ITEMS = "inactive_items"
    }
}