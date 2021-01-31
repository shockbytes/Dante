package at.shockbytes.dante.core.book

import android.os.Parcelable
import at.shockbytes.dante.util.HexColor
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookLabel(
    val bookId: BookId,
    val title: String,
    val hexColor: HexColor
) : Parcelable {

    fun withBookId(bookId: BookId): BookLabel {
        return copy(bookId = bookId)
    }

    companion object {

        fun unassignedLabel(title: String, hexColor: HexColor): BookLabel {
            return BookLabel(BookIds.default(), title, hexColor)
        }
    }
}