package at.shockbytes.dante.core.book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookId(val value: Long) : Parcelable {

    fun isValid(): Boolean = value > -1

    companion object {

        private val defaultValue = BookId(value = -1L)
        fun default() = defaultValue
    }
}
