package at.shockbytes.dante.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
@Parcelize
data class BookSearchItem(
    val bookId: Long,
    val title: String,
    val author: String,
    val thumbnailAddress: String?,
    val isbn: String
) : Parcelable