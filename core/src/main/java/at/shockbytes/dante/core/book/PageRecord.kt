package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
data class PageRecord(
    val bookId: BookId,
    val fromPage: Int,
    val toPage: Int,
    val timestamp: Long
) : Parcelable {

    val diffPages: Int
        get() = toPage - fromPage

    val dateTime: DateTime
        get() = DateTime(timestamp)
}