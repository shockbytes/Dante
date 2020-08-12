package at.shockbytes.dante.ui.custom

import android.content.Context
import android.view.View
import at.shockbytes.dante.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dante_marker_view.*

class DanteMarkerView(
        context: Context
): MarkerView(context, R.layout.dante_marker_view), LayoutContainer {

    override val containerView: View?
        get() = this

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        e?.y?.toInt()?.let { pages ->
            tv_dante_marker_view.text = context.getString(R.string.pages_formatted, pages)
        }

        super.refreshContent(e, highlight)
    }
}