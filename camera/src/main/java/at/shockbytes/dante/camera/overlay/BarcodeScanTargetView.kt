package at.shockbytes.dante.camera.overlay

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import at.shockbytes.dante.camera.R

class BarcodeScanTargetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var scanTargetSize: Float = 0f
    private var scanTargetOutlineWidth: Float = 0f
    private var cornerRadius: Float = 0f
    @ColorInt
    private var bgColor: Int = 0

    private val center: Pair<Float, Float>
        get() = Pair(width / 2f, height / 2f)

    private val scanTargetPaint: Paint
        get() = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            flags = Paint.ANTI_ALIAS_FLAG
        }

    private val scanTargetOutlinePaint: Paint
        get() = Paint().apply {
            color = Color.WHITE
            flags = Paint.ANTI_ALIAS_FLAG
        }

    private var drawingBitmap: Bitmap? = null
    private var drawingCanvas: Canvas? = null

    init {
        setWillNotDraw(false)

        context.theme.obtainStyledAttributes(attrs, R.styleable.BarcodeScanTargetView, defStyle, 0).run {
            initializeWithAttributes(this)
            this.recycle()
        }
    }

    private fun initializeWithAttributes(attributes: TypedArray) {

        scanTargetSize = attributes.getDimension(R.styleable.BarcodeScanTargetView_target_size, 0f)
        cornerRadius = attributes.getDimension(R.styleable.BarcodeScanTargetView_corner_radius, 0f)
        scanTargetOutlineWidth = attributes.getDimension(R.styleable.BarcodeScanTargetView_target_outline_width, 0f)

        val bgColorRes = attributes.getResourceId(R.styleable.BarcodeScanTargetView_bg_color, R.color.scan_target_background)
        bgColor = ContextCompat.getColor(context, bgColorRes)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isViewMeasured()) {

            if (isDrawingCacheEmpty()) {
                createCachedCanvasAndBitmap()
            }

            clearBackground(drawingCanvas)
            drawBackground(drawingCanvas)
            drawScanTargetOutline(drawingCanvas)
            drawScanTarget(drawingCanvas)

            drawingBitmap?.let { bitmap ->
                canvas?.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    private fun isViewMeasured(): Boolean {
        return measuredHeight > 0 && measuredWidth > 0
    }

    private fun isDrawingCacheEmpty(): Boolean {
        return drawingBitmap == null || drawingCanvas == null
    }

    private fun createCachedCanvasAndBitmap() {

        if (drawingBitmap != null) {
            drawingBitmap?.recycle()
        }

        drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            drawingCanvas = Canvas(it)
        }
    }

    private fun clearBackground(c: Canvas?) {
        c?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    private fun drawBackground(c: Canvas?) {
        c?.drawColor(bgColor)
    }

    private fun drawScanTarget(c: Canvas?) {
        if (scanTargetSize > 0) {
            val (centerX, centerY) = center
            val scanTargetHalf = scanTargetSize.div(2)

            c?.drawRoundRect(
                centerX - scanTargetHalf,
                centerY - scanTargetHalf,
                centerX + scanTargetHalf,
                centerY + scanTargetHalf,
                cornerRadius,
                cornerRadius,
                scanTargetPaint
            )
        }
    }

    private fun drawScanTargetOutline(c: Canvas?) {
        if (scanTargetSize > 0) {
            val (centerX, centerY) = center
            val scanTargetHalf = scanTargetSize.div(2)

            c?.drawRoundRect(
                centerX - scanTargetHalf - scanTargetOutlineWidth,
                centerY - scanTargetHalf - scanTargetOutlineWidth,
                centerX + scanTargetHalf + scanTargetOutlineWidth,
                centerY + scanTargetHalf + scanTargetOutlineWidth,
                cornerRadius,
                cornerRadius,
                scanTargetOutlinePaint
            )
        }
    }
}