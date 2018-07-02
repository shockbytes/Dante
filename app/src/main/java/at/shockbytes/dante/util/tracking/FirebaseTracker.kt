package at.shockbytes.dante.util.tracking

import android.content.Context
import android.os.Bundle
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.book.BookEntity
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author Martin Macheiner
 * Date: 12-Jun-18.
 */
class FirebaseTracker(context: Context): Tracker {

    private val fbAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun trackOnScanBook() {
        trackEvent("bookScan", createTrackEventData(Pair("scan_clicked", 1)))
    }

    override fun trackOnScanBookCanceled() {
        trackEvent("bookScanCanceled", createTrackEventData(Pair("scan_canceled", 1)))
    }

    override fun trackOnBookManuallyEntered() {
        trackEvent("bookScanManuallyEntered",
                createTrackEventData(Pair("book_manually_entered", 1)))
    }

    override fun trackOnFoundBookCanceled() {
        trackEvent("bookScanFoundCanceled",
                createTrackEventData(Pair("found_book_cancelled", 1)))
    }

    override fun trackOnBookShared() {
        trackEvent("shareBook", createTrackEventData(Pair("share_book", 1)))
    }

    override fun trackOnBackupMade() {
        trackEvent("backupMade", createTrackEventData(Pair("backupMade", 1)))
    }

    override fun trackOnBackupRestored() {
        trackEvent("backupRestored", createTrackEventData(Pair("backupRestored", 1)))
    }

    override fun trackOnBookScanned(b: BookEntity, viaSearchInterface: Boolean) {
        val data = createTrackEventData(
                Pair("author", b.author),
                Pair("language", b.language ?: "NA"),
                Pair("pages", b.pageCount),
                Pair("viaSearch", viaSearchInterface))
        trackEvent("bookScanned", data)
    }

    override fun trackOnBookMovedToDone(b: BookEntity) {
        val duration = b.endDate - b.startDate
        trackEvent("bookFinished", createTrackEventData(Pair("duration", duration)))
    }

    override fun trackOnDownloadError(reason: String) {
        trackEvent("bookScanDownloadError", createTrackEventData(
                Pair("found_book_download_error", 1),
                Pair("found_book_download_error_reason", reason)))
    }

    override fun trackGoogleLogin(login: Boolean) {
        trackEvent("googleLogin", createTrackEventData(Pair("isLoggedIn", login)))
    }

    override fun trackRatingEvent(rating: Int) {
        trackEvent("bookRating", createTrackEventData(Pair("rated", rating)))
    }

    private fun createTrackEventData(vararg entries: Pair<String, Any>): Bundle {
        val data = Bundle()
        entries.forEach { (key, value) ->
            data.putString(key, value.toString())
        }
        return data
    }

    private fun trackEvent(key: String, data: Bundle) {
        // Do not log in debug mode!
        if (!BuildConfig.DEBUG) {
            fbAnalytics.logEvent(key, data)
        }
    }

}