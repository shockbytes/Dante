package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.ui.adapter.stats.model.LabelStatsItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_labels.*


class BookStatsLabelsViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.LabelStats) {
            when (this) {
                BookStatsItem.LabelStats.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.LabelStats.Present -> {
                    showLabelsCharts(labels)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_stats_labels_empty.setVisible(true)
        chart_item_stats_labels.setVisible(false)
    }

    private fun showLabelsCharts(labels: Map<LabelStatsItem, Int>) {
        item_stats_labels_empty.setVisible(false)
        chart_item_stats_labels.setVisible(true)

        val entries = labels.values.mapIndexed { index, count ->
            BarEntry(index.toFloat(), count.toFloat())
        }

        val barDataSet = BarDataSet(entries, "").apply {
            setColors(*labels.map { it.key.color }.toIntArray())
            setDrawValues(false)
            setDrawIcons(false)
        }

        chart_item_stats_labels.apply {
            description.isEnabled = false
            legend.isEnabled = false

            setDrawGridBackground(false)
            setScaleEnabled(false)
            setTouchEnabled(false)

            xAxis.apply {

                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = entries.size
                setDrawAxisLine(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawGridBackground(false)
                textColor = ContextCompat.getColor(containerView.context, R.color.colorPrimaryText)
                valueFormatter = IndexAxisValueFormatter(labels.map { it.key.title })
            }

            getAxis(YAxis.AxisDependency.LEFT).apply {
                isEnabled = false
                setDrawAxisLine(false)
                setDrawGridLines(false)
                setDrawZeroLine(false)
                setDrawAxisLine(false)
            }
            getAxis(YAxis.AxisDependency.RIGHT).apply {
                isEnabled = true
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(containerView.context, R.color.colorPrimaryText)
            }

            setFitBars(true)

            data = BarData(barDataSet)
            animateXY(400, 400)
            invalidate()
        }
    }
}