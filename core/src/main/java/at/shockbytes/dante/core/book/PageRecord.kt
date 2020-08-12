package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PageRecord(
        val bookId: Long,
        val fromPage: Int,
        val toPage: Int,
        val timestamp: Long
) : Parcelable