package at.shockbytes.dante.backup.model

import androidx.annotation.DrawableRes
import at.shockbytes.dante.R

enum class BackupStorageProvider(val acronym: String, @DrawableRes val icon: Int) {

    UNKNOWN("na", R.drawable.ic_unknown),
    GOOGLE_DRIVE("gdrive", R.drawable.ic_google_drive),
    SHOCKBYTES_SERVER("shock_server", R.drawable.ic_shockbytes);

    companion object {
        fun byAcronym(acronym: String): BackupStorageProvider {
            return values().find { it.acronym == acronym } ?: UNKNOWN
        }
    }
}