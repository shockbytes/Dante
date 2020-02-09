package at.shockbytes.dante.navigation

import android.os.Parcelable
import at.shockbytes.dante.core.book.BookEntity
import kotlinx.android.parcel.Parcelize

sealed class Destination {

    data class BookDetail(val info: BookDetailInfo) : Destination() {

        @Parcelize
        data class BookDetailInfo(
            val id: Long,
            val title: String
        ) : Parcelable
    }

    data class Share(val bookEntity: BookEntity) : Destination()

    data class Main(
        val bookDetailInfo: BookDetail.BookDetailInfo? = null,
        val openCameraAfterLaunch: Boolean = false
    ) : Destination()

    object Search : Destination()
    data class ManualAdd(val updatedBookEntity: BookEntity? = null) : Destination()
    object Statistics : Destination()
    object Timeline : Destination()
    object Backup : Destination()
    object Settings : Destination()

    object BarcodeScanner : Destination()

    data class Notes(val notesBundle: NotesBundle) : Destination()
}
