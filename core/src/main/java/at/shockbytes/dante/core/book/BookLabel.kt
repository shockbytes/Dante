package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookLabel(
    val title: String,
    val hexColor: String
): Parcelable