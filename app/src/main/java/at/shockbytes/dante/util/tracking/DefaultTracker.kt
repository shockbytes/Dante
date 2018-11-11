package at.shockbytes.dante.util.tracking

import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.util.tracking.backend.TrackingBackend

/**
 * @author  Martin Macheiner
 * Date:    30.08.2018
 */
class DefaultTracker(private val tb: TrackingBackend): Tracker {

    override fun trackOnScanBook() {
        tb.trackEvent("bookScan", tb.createTrackEventData(Pair("scan_clicked", 1)))
    }

    override fun trackOnScanBookCanceled() {
        tb.trackEvent("bookScanCanceled", tb.createTrackEventData(Pair("scan_canceled", 1)))
    }

    override fun trackOnBookManuallyEntered() {
        tb.trackEvent("bookScanManuallyEntered",
                tb.createTrackEventData(Pair("book_manually_entered", 1)))
    }

    override fun trackOnFoundBookCanceled() {
        tb.trackEvent("bookScanFoundCanceled",
                tb.createTrackEventData(Pair("found_book_cancelled", 1)))
    }

    override fun trackOnBookShared() {
        tb.trackEvent("shareBook", tb.createTrackEventData(Pair("share_book", 1)))
    }

    override fun trackOnBackupMade() {
        tb.trackEvent("backupMade", tb.createTrackEventData(Pair("backupMade", 1)))
    }

    override fun trackOnBackupRestored() {
        tb.trackEvent("backupRestored", tb.createTrackEventData(Pair("backupRestored", 1)))
    }

    override fun trackOnBookScanned(b: BookEntity, viaSearchInterface: Boolean) {
        val data = tb.createTrackEventData(
                Pair("author", b.author),
                Pair("language", b.language ?: "NA"),
                Pair("pages", b.pageCount),
                Pair("viaSearch", viaSearchInterface))
        tb.trackEvent("bookScanned", data)
    }

    override fun trackOnBookMovedToDone(b: BookEntity) {
        val duration = b.endDate - b.startDate
        tb.trackEvent("bookFinished", tb.createTrackEventData(Pair("duration", duration)))
    }

    override fun trackOnDownloadError(reason: String) {
        tb.trackEvent("bookScanDownloadError", tb.createTrackEventData(
                Pair("found_book_download_error", 1),
                Pair("found_book_download_error_reason", reason)))
    }

    override fun trackGoogleLogin(login: Boolean) {
        tb.trackEvent("googleLogin", tb.createTrackEventData(Pair("isLoggedIn", login)))
    }

    override fun trackRatingEvent(rating: Int) {
        tb.trackEvent("bookRating", tb.createTrackEventData(Pair("rated", rating)))
    }

    override fun trackOnClickSupporterBadgePage() {
        tb.trackEvent("supporterBadgeClick", tb.createTrackEventData(Pair("supporterBadgeClick", 1)))
    }

    override fun trackBuySupporterBadge(badgeType: String) {
        tb.trackEvent("buySupporterBadge", tb.createTrackEventData(Pair("badgeType", badgeType)))
    }

    override fun trackOnBookAddManually() {
        tb.trackEvent("bookAddManually", tb.createTrackEventData(Pair("bookAddManually", 1)))
    }


}