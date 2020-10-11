package at.shockbytes.dante.ui.custom.bookspages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.custom.DanteMarkerView
import at.shockbytes.dante.util.setVisible
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.pages_diagram_view.view.*


class BooksAndPagesDiagramView @JvmOverloads constructor(
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

    var action: BooksAndPagesDiagramAction = BooksAndPagesDiagramAction.Gone
        set(value) {
            field = value
            setActionVisibility(value)
        }

    val actionView: View
        get() {
            return when (action) {
                is BooksAndPagesDiagramAction.Overflow -> iv_page_record_overflow
                is BooksAndPagesDiagramAction.Action -> btn_page_record_action
                is BooksAndPagesDiagramAction.Gone -> throw IllegalStateException("No action view for action type GONE")
            }
        }

    fun setData(
            dataPoints: List<BooksAndPageRecordDataPoint>,
            diagramOptions: BooksAndPagesDiagramOptions = BooksAndPagesDiagramOptions(),
            options: MarkerViewOptions
    ) {

        val formattedDates = dataPoints.map { it.formattedDate }
        val dataSet = createDataSet(createDataSetEntries(dataPoints, diagramOptions.initialZero))

        styleChartAndSetData(dataSet, options, formattedDates, diagramOptions.isZoomable)
    }

    private fun createDataSetEntries(
            dataPoints: List<BooksAndPageRecordDataPoint>,
            initialZero: Boolean
    ): List<Entry> {
        return dataPoints
                .mapIndexed { index, dp ->
                    Entry(index.inc().toFloat(), dp.value.toFloat())
                }
                .toMutableList()
                .apply {
                    if (initialZero) {
                        add(0, BarEntry(0f, 0f)) // Initial entry
                    }
                }
    }

    private fun createDataSet(entries: List<Entry>): LineDataSet {
        return LineDataSet(entries, "").apply {
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
    }

    private fun styleChartAndSetData(
            dataSet: LineDataSet,
            markerViewOptions: MarkerViewOptions,
            formattedDates: List<String>,
            isZoomable: Boolean
    ) {
        chart.apply {
            // Clear old values first, might be null (Java...)
            data?.clearValues()

            description.isEnabled = false
            legend.isEnabled = false

            setTouchEnabled(true)
            setDrawGridBackground(false)
            setScaleEnabled(isZoomable)
            isDragEnabled = isZoomable

            xAxis.apply {
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = formattedDates.size / 2
                setDrawAxisLine(false)
                labelRotationAngle = -30f
                textSize = 8f
                setDrawGridLines(false)
                typeface = ResourcesCompat.getFont(context, R.font.montserrat)
                setDrawAxisLine(false)
                setDrawGridBackground(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
                valueFormatter = IndexAxisValueFormatter(formattedDates)
            }

            getAxis(YAxis.AxisDependency.LEFT).apply {
                isEnabled = true
                setDrawLimitLinesBehindData(true)
                setDrawGridLines(false)
                setDrawZeroLine(false)
                setDrawAxisLine(false)
                typeface = ResourcesCompat.getFont(context, R.font.montserrat)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
            }
            getAxis(YAxis.AxisDependency.RIGHT).apply {
                isEnabled = false
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
            }

            setDrawMarkers(true)
            marker = DanteMarkerView(context, chart, markerViewOptions)

            data = LineData(dataSet)
            invalidate()
        }
    }

    fun registerOnActionClick(clickAction: () -> Unit) {
        when (action) {
            is BooksAndPagesDiagramAction.Overflow -> iv_page_record_overflow.setOnClickListener { clickAction() }
            is BooksAndPagesDiagramAction.Action -> btn_page_record_action.setOnClickListener { clickAction() }
            is BooksAndPagesDiagramAction.Gone -> Unit // Do nothing
        }
    }

    private fun setActionVisibility(value: BooksAndPagesDiagramAction) {
        when (value) {
            BooksAndPagesDiagramAction.Overflow -> {
                iv_page_record_overflow.setVisible(true)
                btn_page_record_action.setVisible(false)
            }
            BooksAndPagesDiagramAction.Gone -> {
                iv_page_record_overflow.setVisible(false)
                btn_page_record_action.setVisible(false)
            }
            is BooksAndPagesDiagramAction.Action -> {
                iv_page_record_overflow.setVisible(false)
                btn_page_record_action.apply {
                    setVisible(true)
                    text = value.title
                }
            }
        }
    }

    fun readingGoal(value: Int?, offsetType: LimitLineOffsetType) {

        // Anyway, remove all limit lines
        chart.getAxis(YAxis.AxisDependency.LEFT).apply {
            removeAllLimitLines()
            setDrawGridLines(false)
        }

        if (value != null) {
            createLimitLine(value.toFloat())
                    .let(::addLimitLineToChart)

            checkLineBoundaries(value.toFloat(), offsetType)
        }
    }

    private fun checkLineBoundaries(value: Float, offsetType: LimitLineOffsetType) {
        val yAxis = chart.getAxis(YAxis.AxisDependency.LEFT)

        when (yAxis.isLimitLineShown(value)) {
            LimitLinePosition.EXCEEDS_UPPER_BOUND -> {
                yAxis.axisMaximum = value.plus(offsetType.offset)
            }
            LimitLinePosition.EXCEEDS_LOWER_BOUND -> {
                yAxis.axisMinimum = value.minus(offsetType.offset)
            }
            LimitLinePosition.IS_VISIBLE -> Unit
        }
    }

    private enum class LimitLinePosition {
        EXCEEDS_UPPER_BOUND,
        EXCEEDS_LOWER_BOUND,
        IS_VISIBLE
    }

    private fun YAxis.isLimitLineShown(limit: Float): LimitLinePosition {
        return when {
            limit <= axisMinimum -> LimitLinePosition.EXCEEDS_LOWER_BOUND
            limit >= axisMaximum -> LimitLinePosition.EXCEEDS_UPPER_BOUND
            else -> LimitLinePosition.IS_VISIBLE
        }
    }

    private fun createLimitLine(value: Float): LimitLine {
        return LimitLine(value, context.getString(R.string.reading_goal)).apply {
            lineColor = ContextCompat.getColor(context, R.color.tabcolor_done)
            lineWidth = 0.8f
            enableDashedLine(20f, 20f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            typeface = ResourcesCompat.getFont(context, R.font.montserrat)
            textSize = 10f
            textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
        }
    }

    private fun addLimitLineToChart(limitLine: LimitLine) {
        chart.getAxis(YAxis.AxisDependency.LEFT).apply {
            setDrawGridLines(true)
            addLimitLine(limitLine)
        }
        invalidate()
    }

    enum class LimitLineOffsetType(val offset: Int) {
        PAGES(offset = 10),
        BOOKS(offset = 1)
    }
}