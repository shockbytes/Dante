package at.shockbytes.dante.ui.custom.rbc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class RelativeBarChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint().apply {
        color = Color.GRAY
        isAntiAlias = true
    }

    private var drawItems: List<DrawItem> = listOf()

    fun setChartData(data: RelativeBarChartData) {
        drawItems = buildDrawItems(data, width.toFloat())
        invalidate()
    }

    private fun buildDrawItems(data: RelativeBarChartData, parentWidth: Float): List<DrawItem> {

        if (data.size == 1) {
            return listOf(DrawItem.SingleItem(0f, parentWidth, createPaint(data.filteredEntries[0].color)))
        }

        return data.filteredEntries.mapIndexed { index, e ->

            when (index) {
                0 -> {
                    // Handle first item
                    val itemWidth = (e.value / data.absoluteValue) * parentWidth
                    DrawItem.FirstItem(0f, itemWidth, createPaint(e.color))
                }
                (data.size - 1) -> {
                    // Handle last item
                    val startX = (data.startValueOf(index) / data.absoluteValue) * parentWidth
                    val itemWidth = (e.value / data.absoluteValue) * parentWidth
                    DrawItem.LastItem(startX, startX + itemWidth, createPaint(e.color))
                }
                else -> {
                    // Item in between
                    val startX = (data.startValueOf(index) / data.absoluteValue) * parentWidth
                    val itemWidth = (e.value / data.absoluteValue) * parentWidth

                    DrawItem.RegularItem(startX, startX + itemWidth, createPaint(e.color))
                }
            }
        }
    }

    private fun createPaint(colorRes: Int): Paint {
        return Paint().apply {
            color = ContextCompat.getColor(context, colorRes)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            drawBackground(canvas)
            drawItems(canvas)
        }
    }

    private fun drawItems(canvas: Canvas) {
        drawItems.forEach { drawItem ->
            drawItem.drawItem(canvas, width.toFloat(), height.toFloat())
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            height.div(2f),
            height.div(2f),
            bgPaint
        )
    }

    private sealed class DrawItem {

        abstract val startX: Float
        abstract val endX: Float
        abstract val paint: Paint

        abstract fun drawItem(canvas: Canvas, parentWidth: Float, parentHeight: Float)

        data class FirstItem(
            override val startX: Float,
            override val endX: Float,
            override val paint: Paint
        ) : DrawItem() {

            override fun drawItem(canvas: Canvas, parentWidth: Float, parentHeight: Float) {

                canvas.drawRoundRect(
                    RectF(startX, 0f, endX, parentHeight),
                    parentHeight.div(2f),
                    parentHeight.div(2f),
                    paint
                )

                canvas.drawRect(
                    RectF(endX - parentHeight.div(2), 0f, endX, parentHeight),
                    paint
                )
            }
        }

        data class LastItem(
            override val startX: Float,
            override val endX: Float,
            override val paint: Paint
        ) : DrawItem() {
            override fun drawItem(canvas: Canvas, parentWidth: Float, parentHeight: Float) {

                canvas.drawRoundRect(
                    RectF(startX, 0f, endX, parentHeight),
                    parentHeight.div(2f),
                    parentHeight.div(2f),
                    paint
                )

                canvas.drawRect(
                    RectF(startX, 0f, startX + parentHeight.div(2), parentHeight),
                    paint
                )
            }
        }

        data class RegularItem(
            override val startX: Float,
            override val endX: Float,
            override val paint: Paint
        ) : DrawItem() {
            override fun drawItem(canvas: Canvas, parentWidth: Float, parentHeight: Float) {

                canvas.drawRect(
                    RectF(startX, 0f, endX, parentHeight),
                    paint
                )
            }
        }

        data class SingleItem(
            override val startX: Float,
            override val endX: Float,
            override val paint: Paint
        ) : DrawItem() {
            override fun drawItem(canvas: Canvas, parentWidth: Float, parentHeight: Float) {

                canvas.drawRoundRect(
                    RectF(startX, 0f, endX, parentHeight),
                    parentWidth.div(2f),
                    parentWidth.div(2f),
                    paint
                )
            }
        }
    }
}
