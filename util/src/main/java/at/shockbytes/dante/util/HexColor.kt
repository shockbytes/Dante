package at.shockbytes.dante.util

class HexColor private constructor(
    private val color: String
) {

    fun asString(): String = color

    companion object {

        /**
         * Test if the format is correct later
         */
        fun ofString(hexColorString: String): HexColor {
            return HexColor(hexColorString)
        }
    }
}
