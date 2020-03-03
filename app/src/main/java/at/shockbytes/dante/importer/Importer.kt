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

    GOODREADS_CSV(
        R.string.import_goodreads_title,
        R.drawable.ic_import_goodreads,
        R.string.import_goodreads_description,
        mimeType = "text/csv",
        stability = Stability.BETA
    ),
    DANTE_CSV(
        R.string.app_name,
        R.drawable.ic_brand_app_logo,
        R.string.import_dante_description,
        mimeType = "text/csv",
        stability = Stability.RELEASE
    )
}