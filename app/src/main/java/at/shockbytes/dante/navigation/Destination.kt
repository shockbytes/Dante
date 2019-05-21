package at.shockbytes.dante.navigation

import android.os.Parcelable
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.ui.activity.BookRetrievalActivity
import kotlinx.android.parcel.Parcelize

sealed class Destination {
    class BookDetail(val info: BookDetailInfo) : Destination() {

        @Parcelize
        data class BookDetailInfo(
            val id: Long,
            val title: String
        ) : Parcelable
    }

    class Share(val bookEntity: BookEntity) : Destination()
    class Retrieval(
        val type: BookRetrievalActivity.RetrievalType,
        val query: String?
    ) : Destination()

    class Main(
        val bookDetailInfo: BookDetail.BookDetailInfo? = null,
        val openCameraAfterLaunch: Boolean = false
    ) : Destination()

    object Search : Destination()
    object ManualAdd : Destination()
    object Statistics : Destination()
    object Backup : Destination()
    object Settings : Destination()
}