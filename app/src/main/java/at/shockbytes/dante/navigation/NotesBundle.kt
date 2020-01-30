package at.shockbytes.dante.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotesBundle(
    val title: String,
    val thumbnailUrl: String?,
    val notes: String
) : Parcelable