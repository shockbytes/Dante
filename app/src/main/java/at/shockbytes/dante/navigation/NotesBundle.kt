package at.shockbytes.dante.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotesBundle(
    val title: String,
    val thumbnailUrl: String?,
    val notes: String
) : Parcelable