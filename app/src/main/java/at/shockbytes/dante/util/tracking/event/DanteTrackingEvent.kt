package at.shockbytes.dante.util.tracking.event

import at.shockbytes.dante.book.BookEntity

sealed class DanteTrackingEvent(val name: String, val props: List<TrackingProperty> = listOf()) {

    class ScanBookEvent : DanteTrackingEvent("bookScan",
            listOf(TrackingProperty("scan_clicked", 1)))

    class ScanBookCanceledEvent : DanteTrackingEvent("bookScanCanceled",
            listOf(TrackingProperty("scan_canceled", 1)))

    class AddByTitleEvent : DanteTrackingEvent("bookScanManuallyEntered",
            listOf(TrackingProperty("book_manually_entered", 1)))

    class FoundBookCanceledEvent : DanteTrackingEvent("bookScanFoundCanceled",
            listOf(TrackingProperty("found_book_cancelled", 1)))

    class BookSharedEvent : DanteTrackingEvent("shareBook",
            listOf(TrackingProperty("share_book", 1)))

    class BackupMadeEvent : DanteTrackingEvent("backupMade",
            listOf(TrackingProperty("backupMade", 1)))

    class BackupRestoredEvent : DanteTrackingEvent("backupRestored",
            listOf(TrackingProperty("backupRestored", 1)))

    class BookScannedEvent(b: BookEntity, viaSearchInterface: Boolean = false) : DanteTrackingEvent("bookScanned",
            listOf(
                    TrackingProperty("author", b.author),
                    TrackingProperty("language", b.language ?: "NA"),
                    TrackingProperty("pages", b.pageCount),
                    TrackingProperty("viaSearch", viaSearchInterface)
            ))

    class BookFinishedEvent(b: BookEntity) : DanteTrackingEvent("bookFinished",
            listOf(TrackingProperty("duration", b.endDate - b.startDate)))

    class BookDownloadErrorEvent(reason: String) : DanteTrackingEvent("bookScanDownloadError",
            listOf(
                    TrackingProperty("found_book_download_error", 1),
                    TrackingProperty("found_book_download_error_reason", reason)))

    class GoogleLoginEvent(isLoggedIn: Boolean) : DanteTrackingEvent("googleLogin",
            listOf(TrackingProperty("isLoggedIn", isLoggedIn)))

    class RatingEvent(rating: Int) : DanteTrackingEvent("bookRating",
            listOf(TrackingProperty("rated", rating)))

    class ShowSupporterBadgePage : DanteTrackingEvent("showSupporterBadge",
            listOf(TrackingProperty("supporterBadgeClick", 1)))

    class BuySupporterBadgeEvent(badgeType: String) : DanteTrackingEvent("buySupporterBadge",
            listOf(TrackingProperty("badgeType", badgeType)))

    class OpenManualAddViewEvent : DanteTrackingEvent("bookAddManually",
            listOf(TrackingProperty("bookAddManually", 1)))

    class DarkModeChangeEvent(from: Boolean, to: Boolean) : DanteTrackingEvent("darkModeChanged",
            listOf(TrackingProperty("from", from), TrackingProperty("to", to)))

}