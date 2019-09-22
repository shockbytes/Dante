
package at.shockbytes.dante.camera.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt

class BarcodeBoundsOverlay {

    private var host: BarcodeOverlayView? = null

    private val cornerRadius = 12f
    private val boxPadding = 0
    private var currentBoundingBox: RectF? = null

    private val boundingBoxBorderPaint = Paint().apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val boundingBoxFillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        setBoundingBoxColor(Color.WHITE)
    }

    private fun setBoundingBoxColor(@ColorInt color: Int) {
        boundingBoxBorderPaint.color = color
        boundingBoxBorderPaint.alpha = 153
        boundingBoxFillPaint.color = color
        boundingBoxFillPaint.alpha = 51
        host?.invalidate()
    }

    fun showBarcodeObject(barcodeObject: BarcodeObject?) {

        currentBoundingBox = barcodeObject?.boundingBox?.apply {
            left -= boxPadding
            top -= boxPadding
            right += boxPadding
            bottom += boxPadding
        }

        host?.invalidate()
    }

    fun onDraw(canvas: Canvas) {
        currentBoundingBox?.let { box ->
            canvas.drawRoundRect(box, cornerRadius, cornerRadius, boundingBoxFillPaint)
            canvas.drawRoundRect(box, cornerRadius, cornerRadius, boundingBoxBorderPaint)
        }
    }

    fun attachToView(barcodeOverlayView: BarcodeOverlayView) {
        this.host = barcodeOverlayView
    }
}