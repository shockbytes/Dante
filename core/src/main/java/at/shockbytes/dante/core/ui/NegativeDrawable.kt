package at.shockbytes.dante.core.ui

import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable

class NegativeDrawable private constructor(val drawable: Drawable) {

    companion object {

        private val negative = floatArrayOf(
            -1.0f, .0f, .0f, .0f, 255.0f,
            .0f, -1.0f, .0f, .0f, 255.0f,
            .0f, .0f, -1.0f, .0f, 255.0f,
            .0f, .0f, .0f, 1.0f, .0f
        )

        fun ofDrawable(drawable: Drawable): NegativeDrawable {

            val negativeDrawable = drawable.apply {
                this.colorFilter = ColorMatrixColorFilter(negative)
            }

            return NegativeDrawable(negativeDrawable)
        }
    }
}