package at.shockbytes.dante.core.book

import android.os.Parcelable
import at.shockbytes.dante.util.DanteUtils
import kotlinx.android.parcel.Parcelize

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
@Parcelize
data class BookEntity(
    var id: Long = -1,
    var title: String = "",
    var subTitle: String = "",
    var author: String = "",
    var state: BookState = BookState.READING,
    var pageCount: Int = 0,
    var publishedDate: String = "",
    var position: Int = 0,
    var isbn: String = "",
    var thumbnailAddress: String? = null,
    var googleBooksLink: String? = null,
    var startDate: Long = 0,
    var endDate: Long = 0,
    var wishlistDate: Long = 0,
    var language: String? = "NA",
    var rating: Int = 0,
    var currentPage: Int = 0,
    var notes: String? = null,
    var summary: String? = null,
    var labels: List<BookLabel> = listOf()
) : Parcelable {

    val reading: Boolean
        get() = state == BookState.READING

    val hasPages: Boolean
        get() = pageCount > 0

    val isAnyTimeInformationAvailable: Boolean
        get() = wishlistDate != 0L || startDate != 0L || endDate != 0L

    fun updateState(state: BookState) {
        this.state = state

        when (state) {
            BookState.READ_LATER -> {
                wishlistDate = System.currentTimeMillis()
                startDate = 0
                endDate = 0
            }
            BookState.READING -> {
                startDate = System.currentTimeMillis()
                endDate = 0
            }
            BookState.READ -> {
                endDate = System.currentTimeMillis()
            }
        }
    }

    val normalizedThumbnailUrl: String?
        get() = thumbnailAddress?.let { url ->
            DanteUtils.checkUrlForHttps(url)
        }
}