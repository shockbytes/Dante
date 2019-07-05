package at.shockbytes.dante.backup.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.util.Priority
import com.google.gson.annotations.SerializedName

enum class BackupStorageProvider(
    val acronym: String,
    val title: String,
    @DrawableRes val icon: Int,
    @StringRes val rationale: Int,
    val priority: Priority
) {

    @SerializedName("na")
    UNKNOWN("na", "na", R.drawable.ic_unknown, R.string.na, Priority.LOW),
    @SerializedName("shock_server")
    SHOCKBYTES_SERVER("shock_server", "Shockbytes Server", R.drawable.ic_shockbytes, R.string.backup_storage_provider_rationale_shockbytes, Priority.HIGH),
    @SerializedName("gdrive")
    GOOGLE_DRIVE("gdrive", "Google Drive", R.drawable.ic_google_drive, R.string.backup_storage_provider_rationale_gdrive, Priority.MEDIUM),
    @SerializedName("ext_storage")
    EXTERNAL_STORAGE("ext_storage", "External Storage", R.drawable.ic_external_storage, R.string.backup_storage_provider_rationale_external_storage, Priority.MEDIUM);

    companion object {

        fun byAcronym(acronym: String): BackupStorageProvider {
            return values().find { it.acronym == acronym } ?: UNKNOWN
        }
    }
}