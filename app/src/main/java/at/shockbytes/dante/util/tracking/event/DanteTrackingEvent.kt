package at.shockbytes.dante.util.tracking.event

import at.shockbytes.dante.book.BookEntity

sealed class DanteTrackingEvent(val name: String, val props: List<TrackingProperty> = listOf()) {

    class BackupMadeEvent : DanteTrackingEvent("backup_made",
            listOf(TrackingProperty("backupMade", 1)))

    class BackupRestoredEvent : DanteTrackingEvent("backup_restored",
            listOf(TrackingProperty("backupRestored", 1)))

    class BookAddedEvent(b: BookEntity, viaSearchInterface: Boolean = false) : DanteTrackingEvent("book_added",
            listOf(
                    TrackingProperty("author", b.author),
                    TrackingProperty("language", b.language ?: "NA"),
                    TrackingProperty("pages", b.pageCount),
                    TrackingProperty("viaSearch", viaSearchInterface)
            ))

    class BookAddedManuallyEvent(b: BookEntity) : DanteTrackingEvent("book_added_manually",
            listOf(
                    TrackingProperty("author", b.author),
                    TrackingProperty("language", b.language ?: "NA"),
                    TrackingProperty("pages", b.pageCount)
            ))

    class OpenTitleSearchEvent : DanteTrackingEvent("open_title_search_modal",
            listOf(TrackingProperty("book_manually_entered", 1)))

    class FoundBookCanceledEvent : DanteTrackingEvent("book_found_but_canceled",
            listOf(TrackingProperty("found_book_canceled", 1)))

    class BookSharedEvent : DanteTrackingEvent("book_shared",
            listOf(TrackingProperty("share_book", 1)))

    class RatingEvent(rating: Int) : DanteTrackingEvent("book_rating",
            listOf(TrackingProperty("rating", rating)))

    class BookFinishedEvent(b: BookEntity) : DanteTrackingEvent("book_finished",
            listOf(TrackingProperty("duration", b.endDate - b.startDate)))

    class BookDownloadErrorEvent(reason: String) : DanteTrackingEvent("scan_download_error",
            listOf(
                    TrackingProperty("found_book_download_error", 1),
                    TrackingProperty("found_book_download_error_reason", reason)))

    class GoogleLoginEvent(isLoggedIn: Boolean) : DanteTrackingEvent("login_google",
            listOf(TrackingProperty("is_logged_in", isLoggedIn)))

    class OpenManualAddViewEvent : DanteTrackingEvent("open_manual_add",
            listOf(TrackingProperty("open_manual_add_property", 1)))

    class OpenCameraEvent : DanteTrackingEvent("open_camera",
            listOf(TrackingProperty("scan_clicked", 1)))

    class DarkModeChangeEvent(from: Boolean, to: Boolean) : DanteTrackingEvent("change_dark_mode",
            listOf(TrackingProperty("from", from), TrackingProperty("to", to)))
}