package at.shockbytes.dante.backup.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import com.google.gson.annotations.SerializedName

enum class BackupStorageProvider(
    val acronym: String,
    val title: String,
    @DrawableRes val icon: Int,
    @StringRes val rationale: Int
) {

    @SerializedName("na")
    UNKNOWN("na", "na", R.drawable.ic_unknown, R.string.na),
    @SerializedName("gdrive")
    GOOGLE_DRIVE("gdrive", "Google Drive", R.drawable.ic_google_drive, R.string.backup_storage_provider_rationale_gdrive),
    @SerializedName("shock_server")
    SHOCKBYTES_SERVER("shock_server", "Shockbytes Server", R.drawable.ic_shockbytes, R.string.backup_storage_provider_rationale_shockbytes),
    @SerializedName("ext_storage")
    EXTERNAL_STORAGE("ext_storage", R.drawable.ic_external_storage);

    companion object {
        fun byAcronym(acronym: String): BackupStorageProvider {
            return values().find { it.acronym == acronym } ?: UNKNOWN
        }
    }
}