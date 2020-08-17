package at.shockbytes.dante.ui.custom.pages

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.custom.DanteMarkerView
import at.shockbytes.dante.util.setVisible
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.pages_diagram_view.view.*

class PagesDiagramView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.pages_diagram_view, this)
    }

    private val chart: LineChart
        get() = lc_page_records

    var headerTitle: String = ""
        set(value) {
            field = value
            tv_page_record_header.text = value
        }

    var action: PagesDiagramAction = PagesDiagramAction.Gone
        set(value) {
            field = value
            setActionVisibility(value)
        }

    fun registerOnActionClick(cAction: () -> Unit) {
        when (action) {
                PagesDiagramAction.Overflow -> iv_page_record_overflow.setOnClickListener { cAction() }
                PagesDiagramAction.Gone -> Unit // Do nothing
                is PagesDiagramAction.Action -> btn_page_record_action.setOnClickListener { cAction() }
            }
        }

    private fun setActionVisibility(value: PagesDiagramAction) {
        when (value) {
            PagesDiagramAction.Overflow -> {
                iv_page_record_overflow.setVisible(true)
                btn_page_record_action.setVisible(false)
            }
            PagesDiagramAction.Gone -> {
                iv_page_record_overflow.setVisible(false)
                btn_page_record_action.setVisible(false)
            }
            is PagesDiagramAction.Action -> {
                iv_page_record_overflow.setVisible(false)
                btn_page_record_action.apply {
                    setVisible(true)
                    text = value.title
                }
            }
        }
    }

    fun updateData(dataPoints: List<PageRecordDataPoint>, initialZero: Boolean = false) {

        val entries: List<Entry> = dataPoints
                .mapIndexed { index, dp ->
                    Entry(index.inc().toFloat(), dp.page.toFloat())
                }
                .toMutableList()
                .apply {
                    if (initialZero) {
                        add(0, BarEntry(0f, 0f)) // Initial entry
                    }
                }

        val dataSet = LineDataSet(entries, "").apply {
            setColor(ContextCompat.getColor(context, R.color.page_record_data), 255)
            setDrawValues(false)
            setDrawIcons(false)
            setDrawFilled(true)
            setDrawHighlightIndicators(false)
            isHighlightEnabled = true
            setCircleColor(ContextCompat.getColor(context, R.color.page_record_data))
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.page_record_gradient)
        }

        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false

            setDrawGridBackground(false)
            setScaleEnabled(false)
            setTouchEnabled(true)

            xAxis.apply {
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = entries.size / 2
                setDrawAxisLine(false)
                labelRotationAngle = -30f
                textSize = 8f
                setDrawGridLines(false)
                typeface = ResourcesCompat.getFont(context, R.font.montserrat)
                setDrawAxisLine(false)
                setDrawGridBackground(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
                valueFormatter = IndexAxisValueFormatter(dataPoints.map { it.formattedDate })
            }

            getAxis(YAxis.AxisDependency.LEFT).apply {
                isEnabled = false
                setDrawAxisLine(false)
                setDrawGridLines(false)
                setDrawZeroLine(false)
                setDrawAxisLine(false)
            }
            getAxis(YAxis.AxisDependency.RIGHT).apply {
                isEnabled = false
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
            }

            setDrawMarkers(true)
            marker = DanteMarkerView(context)

            data = LineData(dataSet)
            invalidate()
        }
    }
}