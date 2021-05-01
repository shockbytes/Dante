package at.shockbytes.dante.util

import android.graphics.Color
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class HexColor private constructor(
    private val color: String
): Parcelable {

    fun asString(): String = color

    fun asColorInt(): Int = Color.parseColor(color)

    fun asDesaturatedColorInt(desaturateBy: Float): Int {
        return ColorUtils.desaturateAndDevalue(asColorInt(), desaturateBy)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is HexColor) {
            other.color == this.color
        } else false
    }

    override fun toString(): String = color

    override fun hashCode(): Int {
        return color.hashCode()
    }

    companion object {

        /**
         * TODO Test if the format #RRGGBB is correct later
         */
        fun ofString(hexColorString: String): HexColor {
            return HexColor(hexColorString)
        }
    }
}
