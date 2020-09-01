package at.shockbytes.dante.ui.custom.bookspages

import androidx.annotation.StringRes

data class MarkerViewOptions(
        @StringRes val markerTemplateResource: Int,
        val formattedDates: List<String>
) {

    companion object {

        fun ofDataPoints(
                dp: List<BooksAndPageRecordDataPoint>,
                markerTemplateResource: Int
        ): MarkerViewOptions {
            return MarkerViewOptions(markerTemplateResource, dp.map { it.formattedDate })
        }
    }
}