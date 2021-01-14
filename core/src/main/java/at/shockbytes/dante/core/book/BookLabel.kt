package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookLabel(
    val bookId: BookId,
    val title: String,
    val hexColor: String
) : Parcelable {

    fun withBookId(bookId: BookId): BookLabel {
        return copy(bookId = bookId)
    }

    companion object {

        fun unassignedLabel(title: String, hexColor: String): BookLabel {
            return BookLabel(BookIds.default(), title, hexColor)
        }
    }
}