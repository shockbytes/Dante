package at.shockbytes.dante.core.book.realm

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PageRecord(
        val bookId: Long,
        val fromPage: Int,
        val toPage: Int,
        val date: String // TODO Find suitable data type
) : Parcelable {

    val pages: Int
        get() = toPage - fromPage
}