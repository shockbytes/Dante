package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.Languages
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_languages.*

class BookStatsLanguageViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.LanguageDistribution) {
            when (this) {
                BookStatsItem.LanguageDistribution.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.LanguageDistribution.Present -> {
                    showLanguageChart(languages)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_stats_languages_empty.setVisible(true)
        chart_item_stats_language.setVisible(false)
    }

    private fun showLanguageChart(languages: Map<Languages, Int>) {
        item_stats_languages_empty.setVisible(false)
        chart_item_stats_language.setVisible(true)

        val entries = languages.map { (language, books) ->
            val title = containerView.context.getString(language.title)
            val iconDrawable = containerView.context.getDrawable(language.image)
            PieEntry(books.toFloat(), title, iconDrawable)
        }

        val pieDataSet = PieDataSet(entries, "").apply {
            setColors(*ColorTemplate.VORDIPLOM_COLORS)
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = ""
            }
            setDrawIcons(true)
        }

        chart_item_stats_language.apply {
            isDrawHoleEnabled = false
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)

            legend.apply {
                isWordWrapEnabled = true
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                formSize = 20F
                formToTextSpace = 5f
                textColor = ContextCompat.getColor(context, R.color.colorSecondaryText)
                form = Legend.LegendForm.CIRCLE
                textSize = 17f
                orientation = Legend.LegendOrientation.HORIZONTAL
                isWordWrapEnabled = true
                setDrawInside(false)
            }

            data = PieData(pieDataSet)
            animateXY(400, 400)
            invalidate()
        }
    }
}