package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookLabel(
    val bookId: Long,
    val title: String,
    val hexColor: String
) : Parcelable {

    fun withBookId(bookId: Long): BookLabel {
        return copy(bookId = bookId)
    }

    companion object {

        const val UNASSIGNED_LABEL_ID = -1L

        fun unassignedLabel(title: String, hexColor: String): BookLabel {
            return BookLabel(UNASSIGNED_LABEL_ID, title, hexColor)
        }
    }
}