package at.shockbytes.dante.importer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R

enum class Importer(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @StringRes val description: Int
) {

    GOODREADS_CSV(
        R.string.import_goodreads_title,
        R.drawable.ic_import_goodreads,
        R.string.import_goodreads_description
    ),
    DANTE_CSV(
        R.string.app_name,
        R.drawable.ic_brand_app_logo,
        R.string.import_dante_description
    )
}