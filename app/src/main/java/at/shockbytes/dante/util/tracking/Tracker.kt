package at.shockbytes.dante.util.tracking

import at.shockbytes.dante.util.books.Book

/**
 * @author Martin Macheiner
 * Date: 01.06.2017.
 */

interface Tracker {

    fun trackOnScanBook()

    fun trackOnScanBookCanceled()

    fun trackOnBookManuallyEntered()

    fun trackOnFoundBookCanceled()

    fun trackOnBookShared()

    fun trackOnBackupMade()

    fun trackOnBackupRestored()

    fun trackOnBookScanned(b: Book)

    fun trackOnBookMovedToDone(b: Book)

    fun trackOnDownloadError(reason: String)

    fun trackGoogleLogin(login: Boolean)

    fun trackRatingEvent(rating: Int)

}
