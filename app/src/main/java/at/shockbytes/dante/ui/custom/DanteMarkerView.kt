package at.shockbytes.dante.ui.custom

import android.content.Context
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.DanteMarkerViewBinding
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.util.layoutInflater
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.extensions.LayoutContainer

class DanteMarkerView(
    context: Context,
    chartView: Chart<*>,
    private val labelFactory: MarkerViewLabelFactory
) : MarkerView(context, R.layout.dante_marker_view), LayoutContainer {

    private val vb: DanteMarkerViewBinding
        get() = DanteMarkerViewBinding.inflate(context.layoutInflater(), this, true)

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
        vb.tvDanteMarkerView.text = labelFactory.createLabelForValue(context, entry.value)
    }

    private fun handleGenericEntry(entry: Entry) {
        val idx = entry.x.toInt().dec()
        if (idx >= 0) {
            vb.tvDanteMarkerView.text = labelFactory.createLabelForIndex(context, idx)
        }
    }
}