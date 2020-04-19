package at.shockbytes.dante.util

import android.graphics.Color.RGBToHSV
import android.graphics.Color
import android.graphics.Color.HSVToColor
import androidx.annotation.ColorInt

object ColorUtils {

    fun desaturateAndDevalue(@ColorInt src: Int, by: Float): Int {

        if (by < 0 && by > 1) {
            throw IllegalArgumentException("[by] must be in range 0..1, currently: $by")
        }

        val hsv = FloatArray(3)

        RGBToHSV(Color.red(src), Color.green(src), Color.blue(src), hsv)
        hsv[2] -= by
        hsv[1] -= by
        return HSVToColor(hsv)
    }

}