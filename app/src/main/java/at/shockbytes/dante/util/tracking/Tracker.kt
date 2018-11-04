package at.shockbytes.dante.util.tracking

import at.shockbytes.dante.book.BookEntity

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

    fun trackOnBookScanned(b: BookEntity, viaSearchInterface: Boolean = false)

    fun trackOnBookMovedToDone(b: BookEntity)

    fun trackOnDownloadError(reason: String)

    fun trackGoogleLogin(login: Boolean)

    fun trackRatingEvent(rating: Int)

    fun trackOnClickSupporterBadgePage()

    fun trackBuySupporterBadge(badgeType: String)

    fun trackOnBookAddManually()

}
