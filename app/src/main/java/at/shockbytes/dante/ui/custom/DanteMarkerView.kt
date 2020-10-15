package at.shockbytes.dante.ui.custom

import android.content.Context
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dante_marker_view.*

class DanteMarkerView(
    context: Context,
    chartView: Chart<*>,
    private val labelFactory: MarkerViewLabelFactory
) : MarkerView(context, R.layout.dante_marker_view), LayoutContainer {

    init {
        setChartView(chartView)
    }

    override val containerView: View
        get() = this

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        e?.let { entry ->
            when (entry) {
                is PieEntry -> handlePieEntry(entry)
                else -> handleGenericEntry(entry)
            }
        }

        super.refreshContent(e, highlight)
    }

    private fun handlePieEntry(entry: PieEntry) {
        tv_dante_marker_view.text = labelFactory.createLabelForValue(context, entry.value)
    }

    private fun handleGenericEntry(entry: Entry) {
        val idx = entry.x.toInt().dec()
        if (idx >= 0) {
            tv_dante_marker_view.text = labelFactory.createLabelForIndex(context, idx)
        }
    }
}