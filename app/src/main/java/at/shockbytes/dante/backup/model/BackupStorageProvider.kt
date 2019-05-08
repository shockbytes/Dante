package at.shockbytes.dante.backup.model

import androidx.annotation.DrawableRes
import at.shockbytes.dante.R
import com.google.gson.annotations.SerializedName

enum class BackupStorageProvider(val acronym: String, @DrawableRes val icon: Int) {

    @SerializedName("na")
    UNKNOWN("na", R.drawable.ic_unknown),
    @SerializedName("gdrive")
    GOOGLE_DRIVE("gdrive", R.drawable.ic_google_drive),
    @SerializedName("shock_server")
    SHOCKBYTES_SERVER("shock_server", R.drawable.ic_shockbytes);

    companion object {
        fun byAcronym(acronym: String): BackupStorageProvider {
            return values().find { it.acronym == acronym } ?: UNKNOWN
        }
    }
}