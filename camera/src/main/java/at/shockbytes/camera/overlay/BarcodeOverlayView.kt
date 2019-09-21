package at.shockbytes.camera.overlay
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.GuardedBy

class BarcodeOverlayView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : FrameLayout(context, attributeSet, style) {

    private var overlay: BarcodeBoundsOverlay? = null

    init {
        setWillNotDraw(false)
    }

    fun add(overlay: BarcodeBoundsOverlay) {
        this.overlay = overlay
        this.overlay?.attachToView(this)
        invalidate()
    }

    fun clear() {
        overlay = null
        invalidate()
    }

    @GuardedBy("objectLock")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        overlay?.onDraw(canvas)
    }
}