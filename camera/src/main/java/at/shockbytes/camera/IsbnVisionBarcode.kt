package at.shockbytes.camera

import android.graphics.Point
import android.graphics.Rect
import android.util.Size

data class IsbnVisionBarcode(
    val isbn: String,
    val corners: List<Point>?,
    val bounds: Rect?,
    val sourceSize: Size,
    val sourceRotationDegrees: Int
)