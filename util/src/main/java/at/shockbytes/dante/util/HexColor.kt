package at.shockbytes.dante.util

import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HexColor private constructor(
    private val color: String
): Parcelable {

    fun asString(): String = color

    fun asColorInt(): Int = Color.parseColor(color)

    fun asDesaturatedColorInt(desaturateBy: Float): Int {
        return ColorUtils.desaturateAndDevalue(asColorInt(), desaturateBy)
    }

    override fun toString(): String = color

    companion object {

        /**
         * Test if the format is correct later
         */
        fun ofString(hexColorString: String): HexColor {
            return HexColor(hexColorString)
        }
    }
}
