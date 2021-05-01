package at.shockbytes.dante.core.book

import android.os.Parcelable
import at.shockbytes.dante.util.HexColor
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookLabel(
    val bookId: BookId,
    val title: String,
    /**
     * This private field is required to ensure backwards compatibility with already
     * existing backups. Callers use now [labelHexColor] of type [HexColor] instead
     * of working with the raw string.
     */
    private val hexColor: String
) : Parcelable {

    val labelHexColor: HexColor
        get() = HexColor.ofString(hexColor)

    fun withBookId(bookId: BookId): BookLabel {
        return copy(bookId = bookId)
    }

    companion object {

        fun unassignedLabel(title: String, hexColor: HexColor): BookLabel {
            return BookLabel(BookIds.default(), title, hexColor.asString())
        }
    }
}