package at.shockbytes.dante.ui.custom.bookspages

import android.content.Context

class MarkerViewLabelFactory private constructor(
    private val factoryMethod: (Context, Int) -> String?
) {

    fun createLabelForIndex(context: Context, index: Int): String? = factoryMethod(context, index)

    companion object {

        fun ofBooksAndPageRecordDataPoints(
            dp: List<BooksAndPageRecordDataPoint>,
            markerTemplateResource: Int
        ): MarkerViewLabelFactory {
            return MarkerViewLabelFactory { context, index ->
                val (content, date) = dp[index]
                context.getString(markerTemplateResource, content, date)
            }
        }

        fun ofEntries(
            values: List<String>,
            markerTemplateResource: Int
        ): MarkerViewLabelFactory {
            TODO()
            //return MarkerViewLabelFactory(markerTemplateResource, values)
        }
    }
}