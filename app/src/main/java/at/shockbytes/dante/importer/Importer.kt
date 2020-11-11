package at.shockbytes.dante.importer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.util.Stability

enum class Importer(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @StringRes val description: Int,
    val mimeType: String,
    val stability: Stability
) {

    DANTE_EXTERNAL_STORAGE(
        R.string.import_dante_external_title,
        R.drawable.ic_external_storage,
        R.string.import_external_storage_description,
        mimeType = "application/json",
        stability = Stability.RELEASE
    ),
    DANTE_CSV(
        R.string.import_dante_csv_title,
        R.drawable.ic_csv,
        R.string.import_dante_description,
        mimeType = "text/csv",
        stability = Stability.BETA
    ),
    GOODREADS_CSV(
        R.string.import_goodreads_title,
        R.drawable.ic_import_goodreads,
        R.string.import_goodreads_description,
        mimeType = "text/csv",
        stability = Stability.BETA
    ),
}