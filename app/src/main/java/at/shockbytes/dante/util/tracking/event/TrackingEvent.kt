package at.shockbytes.dante.util.tracking.event

import at.shockbytes.dante.book.BookEntity

sealed class TrackingEvent(val name: String, val props: List<TrackingProperty> = listOf()) {

    class ScanBookEvent : TrackingEvent("bookScan",
            listOf(TrackingProperty("scan_clicked", 1)))

    class ScanBookCanceledEvent : TrackingEvent("bookScanCanceled",
            listOf(TrackingProperty("scan_canceled", 1)))

    class AddByTitleEvent : TrackingEvent("bookScanManuallyEntered",
            listOf(TrackingProperty("book_manually_entered", 1)))

    class FoundBookCanceledEvent : TrackingEvent("bookScanFoundCanceled",
            listOf(TrackingProperty("found_book_cancelled", 1)))

    class BookSharedEvent : TrackingEvent("shareBook",
            listOf(TrackingProperty("share_book", 1)))

    class BackupMadeEvent : TrackingEvent("backupMade",
            listOf(TrackingProperty("backupMade", 1)))

    class BackupRestoredEvent : TrackingEvent("backupRestored",
            listOf(TrackingProperty("backupRestored", 1)))

    class BookScannedEvent(b: BookEntity, viaSearchInterface: Boolean = false) : TrackingEvent("bookScanned",
            listOf(
                    TrackingProperty("author", b.author),
                    TrackingProperty("language", b.language ?: "NA"),
                    TrackingProperty("pages", b.pageCount),
                    TrackingProperty("viaSearch", viaSearchInterface)
            ))

    class BookFinishedEvent(b: BookEntity) : TrackingEvent("bookFinished",
            listOf(TrackingProperty("duration", b.endDate - b.startDate)))

    class BookDownloadErrorEvent(reason: String) : TrackingEvent("bookScanDownloadError",
            listOf(
                    TrackingProperty("found_book_download_error", 1),
                    TrackingProperty("found_book_download_error_reason", reason)))

    class GoogleLoginEvent(isLoggedIn: Boolean) : TrackingEvent("googleLogin",
            listOf(TrackingProperty("isLoggedIn", isLoggedIn)))

    class RatingEvent(rating: Int) : TrackingEvent("bookRating",
            listOf(TrackingProperty("rated", rating)))

    class ShowSupporterBadgePage : TrackingEvent("showSupporterBadge",
            listOf(TrackingProperty("supporterBadgeClick", 1)))

    class BuySupporterBadgeEvent(badgeType: String) : TrackingEvent("buySupporterBadge",
            listOf(TrackingProperty("badgeType", badgeType)))

    class OpenManualAddViewEvent : TrackingEvent("bookAddManually",
            listOf(TrackingProperty("bookAddManually", 1)))

    class DarkModeChangeEvent(from: Boolean, to: Boolean) : TrackingEvent("darkModeChanged",
            listOf(TrackingProperty("from", from), TrackingProperty("to", to)))

}