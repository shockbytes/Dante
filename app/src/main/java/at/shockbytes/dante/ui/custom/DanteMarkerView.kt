package at.shockbytes.dante.ui.custom

import android.content.Context
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewOptions
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dante_marker_view.*

class DanteMarkerView(
    context: Context,
    chartView: Chart<*>,
    private val options: MarkerViewOptions
) : MarkerView(context, R.layout.dante_marker_view), LayoutContainer {

    init {
        setChartView(chartView)
    }

    override val containerView: View
        get() = this

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        e?.let { entry ->

            val dateIdx = entry.x.toInt().dec()
            if (dateIdx >= 0) {
                val date = options.formattedDates[dateIdx]
                val content = entry.y.toInt()
                tv_dante_marker_view.text = context.getString(options.markerTemplateResource, content, date)
            }
        }
        super.refreshContent(e, highlight)
    }
}