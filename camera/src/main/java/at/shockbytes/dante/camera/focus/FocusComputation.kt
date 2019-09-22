package at.shockbytes.dante.camera.focus

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FocusComputation(
    private val host: View
) {

    private val focusRect = Rect()
    private val meteringRect = Rect()
    var rotationDegrees: Int? = null

    private val focusSubject = PublishSubject.create<FocusEvent>()
    val onFocusEvent: Observable<FocusEvent> = focusSubject.throttleFirst(200, TimeUnit.MILLISECONDS)

    fun compute(event: MotionEvent) {
        calculateTapArea(focusRect, event.x, event.y, 1f)
        calculateTapArea(meteringRect, event.x, event.y, 1.5f)
        if (focusRect.area() > 0 && meteringRect.area() > 0) {
            focusSubject.onNext(FocusEvent(focusRect, meteringRect))
        }
    }

    private fun Rect.area(): Int {
        return width() * height()
    }

    private fun calculateTapArea(rect: Rect, xv: Float, yv: Float, coefficient: Float) {

        var x = xv
        var y = yv

        val max = 1000
        val min = -1000

        // Default to 300 (1/6th the total area) and scale by the coefficient
        val areaSize = (300 * coefficient).toInt()

        // Rotate the coordinates if the camera orientation is different
        var width = host.width
        var height = host.height

        // Compensate orientation as it's mirrored on preview for forward facing cameras
        val relativeCameraOrientation = rotationDegrees ?: 90
        val temp: Int
        val tempf: Float
        when (relativeCameraOrientation) {
            90,
                // Fall-through
            270 -> {
                // We're horizontal. Swap width/height. Swap x/y.
                temp = width

                width = height
                height = temp
                tempf = x

                x = y
                y = tempf
            }
            else -> {
            }
        }
        when (relativeCameraOrientation) {
            // Map to correct coordinates according to relativeCameraOrientation
            90 -> y = height - y
            180 -> {
                x = width - x
                y = height - y
            }
            270 -> x = width - x
            else -> {
            }
        }
        // Grab the x, y position from within the View and normalize it to -1000 to 1000
        x = min + distance(max, min) * (x / width)
        y = min + distance(max, min) * (y / height)
        // Modify the rect to the bounding area
        rect.top = y.toInt() - areaSize / 2
        rect.left = x.toInt() - areaSize / 2
        rect.bottom = rect.top + areaSize
        rect.right = rect.left + areaSize
        // Cap at -1000 to 1000
        rect.top = rangeLimit(rect.top, max, min)
        rect.left = rangeLimit(rect.left, max, min)
        rect.bottom = rangeLimit(rect.bottom, max, min)
        rect.right = rangeLimit(rect.right, max, min)
    }

    private fun rangeLimit(value: Int, max: Int, min: Int): Int {
        return min(max(value, min), max)
    }

    private fun distance(a: Int, b: Int): Int {
        return abs(a - b)
    }
}