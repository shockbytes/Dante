package at.shockbytes.dante.camera.focus

import android.graphics.Rect

data class FocusEvent(
    val focusRect: Rect,
    val meteringRect: Rect
)