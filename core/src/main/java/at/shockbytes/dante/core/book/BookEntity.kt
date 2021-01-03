package at.shockbytes.dante.core.book

import android.os.Parcelable
import at.shockbytes.dante.util.DanteUtils.checkUrlForHttps
import kotlinx.android.parcel.Parcelize

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 *
 * NOTE:    Would be great if we can get rid off all the vars and make them vals.
 */
@Parcelize
data class BookEntity(
    var id: BookId = BookId.default(),
    val title: String = "",
    val subTitle: String = "",
    val author: String = "",
    var state: BookState = BookState.READING,
    val pageCount: Int = 0,
    val publishedDate: String = "",
    var position: Int = 0,
    val isbn: String = "",
    val thumbnailAddress: String? = null,
    val googleBooksLink: String? = null,
    var startDate: Long = 0,
    var endDate: Long = 0,
    /**
     * Actually `forLaterDate` and should not be confused with BookState.WISHLIST. This mishap
     * is due to the initial naming and cannot be changed without breaking prior backups. So, just
     * treat this as `forLaterDate` and everything is fine
     */
    var wishlistDate: Long = 0,
    val language: String? = "NA",
    val rating: Int = 0,
    val currentPage: Int = 0,
    val notes: String? = null,
    val summary: String? = null,
    val labels: List<BookLabel> = listOf()
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
            BookState.WISHLIST -> {
                startDate = 0
                endDate = 0
                wishlistDate = 0
            }
        }
    }

    val normalizedThumbnailUrl: String?
        get() = thumbnailAddress?.checkUrlForHttps()
}