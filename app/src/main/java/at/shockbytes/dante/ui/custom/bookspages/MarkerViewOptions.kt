package at.shockbytes.dante.ui.custom.bookspages

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry

class MarkerViewOptions private constructor(
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

        fun ofEntries(
            values: List<String>,
            markerTemplateResource: Int
        ): MarkerViewOptions {
            return MarkerViewOptions(markerTemplateResource, values)
        }
    }
}