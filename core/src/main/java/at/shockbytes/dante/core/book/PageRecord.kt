package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class PageRecord(
        val bookId: Long,
        val fromPage: Int,
        val toPage: Int,
        val timestamp: Long
) : Parcelable {

    val diffPages: Int
        get() = toPage - fromPage

    val dateTime: DateTime
        get() = DateTime(timestamp)
}