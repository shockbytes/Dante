package at.shockbytes.dante.util.tracking

import android.content.Context
import android.support.v4.util.Pair
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.util.books.Book
import io.keen.client.android.AndroidKeenClientBuilder
import io.keen.client.java.KeenClient
import io.keen.client.java.KeenProject
import java.util.*


/**
 * @author Martin Macheiner
 * Date: 01.06.2017.
 */

class KeenTracker(context: Context) : Tracker {

    init {
        initializeKeen(context)
    }

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

    override fun trackOnBookScanned(b: Book) {
        val data = createTrackEventData(
                Pair("author", b.author),
                Pair("language", b.language),
                Pair("pages", b.pageCount))
        trackEvent("bookScanned", data)
    }

    override fun trackOnBookMovedToDone(b: Book) {
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

    // -------------------------- Helper methods --------------------------

    private fun initializeKeen(context: Context) {

        // First initialize client
        val client = AndroidKeenClientBuilder(context).build()
        KeenClient.initialize(client)

        // Then initialize project
        val project = KeenProject(PROJECT_ID, WRITE_KEY, READ_KEY)
        KeenClient.client().defaultProject = project
    }

    private fun createTrackEventData(vararg entries: Pair<String, Any>): Map<String, Any> {

        val data = HashMap<String, Any>()
        for (p in entries) {
            data.put(p.first, p.second)
        }
        return data
    }

    private fun trackEvent(event: String, data: Map<String, Any>) {
        // Only track in release version!
        if (!BuildConfig.DEBUG) {
            KeenClient.client().addEventAsync(event, data)
        }
    }

    companion object {

        private val PROJECT_ID = "592eb8de95cfc93b3abe8c33"
        private val READ_KEY = "D067FCFDB2B7D90D34DE0EA4E914C5B8FF695F595A00C07457519F09C8E69A60080CE16B81A868C227579683CBD1EEA6592FFEF2D729E8F746A0920AA997DB999250A503188894C6B82BE711E790F73A3852FEED4DC7EA414558E75B68DA4603"
        private val WRITE_KEY = "0B74F581F4EF0B5424333A41087800CF1CFE15D138D13BAD384B3ED66FB1F32FB725E096D872278284C7A89DF532EF58ED1B9E694D1D20EB6F0CE3A02BBF91466DF75A9FB7A2BCDD5E73916F31CBF9602BF546ADD495C89563FDA1383A267F0B"
    }

}
