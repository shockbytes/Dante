package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.pages_diagram_view.view.*

class PagesDiagramView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.pages_diagram_view, this)
    }

    val chart: LineChart
        get() = lc_page_records

}