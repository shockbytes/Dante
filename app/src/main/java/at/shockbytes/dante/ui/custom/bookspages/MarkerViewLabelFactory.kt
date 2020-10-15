package at.shockbytes.dante.ui.custom.bookspages

import android.content.Context

class MarkerViewLabelFactory private constructor(
    private val indexFactoryMethod: ((Context, Int) -> String?)? = null,
    private val valueFactoryMethod: ((Context, Float) -> String?)? = null
) {

    fun createLabelForIndex(context: Context, index: Int): String? {
        return indexFactoryMethod?.invoke(context, index)
    }

    fun createLabelForValue(context: Context, value: Float): String? {
        return valueFactoryMethod?.invoke(context, value)
    }

    companion object {

        fun ofBooksAndPageRecordDataPoints(
            dp: List<BooksAndPageRecordDataPoint>,
            markerTemplateResource: Int
        ): MarkerViewLabelFactory {
            return MarkerViewLabelFactory(
                indexFactoryMethod = { context, index ->
                    val (content, date) = dp[index]
                    context.getString(markerTemplateResource, content, date)
                }
            )
        }

        fun forPlainEntries(markerTemplateResource: Int): MarkerViewLabelFactory {
            return MarkerViewLabelFactory(
                valueFactoryMethod = { context, value ->
                    context.getString(markerTemplateResource, value.toInt())
                }
            )
        }
    }
}