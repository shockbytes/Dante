package at.shockbytes.tracking.event

sealed class DanteTrackingEvent(
    val name: String,
    val props: List<TrackingProperty> = listOf()
) {

    data class BackupMadeEvent(val backupProvider: String) : DanteTrackingEvent(
        "backup_made",
        listOf(TrackingProperty("backup_provider", backupProvider))
    )

    object InterestedInOnlineStorageEvent : DanteTrackingEvent("interested_in_online_storage")

    data class StartImport(val importer: String) : DanteTrackingEvent(
        "start_import",
        listOf(TrackingProperty("importer_name", importer))
    )

    data class TrackingStateChanged(val state: Boolean) : DanteTrackingEvent(
        "tracking_state_changed",
        listOf(TrackingProperty("state", state))
    )

    data class PickRandomBook(val booksInBacklog: Int) : DanteTrackingEvent(
        "pick_random_book",
        listOf(TrackingProperty("backlog_count", booksInBacklog))
    )

    data class OpenBackupFile(val providerAcronym: String) : DanteTrackingEvent(
        "open_backup_file",
        listOf(TrackingProperty("backup_provider", providerAcronym))
    )

    data class AddSuggestionToWishlist(
        val suggestionId: String,
        val bookTitle: String,
        val suggester: String
    ) : DanteTrackingEvent(
        "add_suggestion_to_wishlist",
        listOf(
            TrackingProperty("suggestion_id", suggestionId),
            TrackingProperty("suggestion_book", bookTitle),
            TrackingProperty("suggestion_suggester", suggester)
        )
    )

    object DisableRandomBookInteraction : DanteTrackingEvent("disable_random_book_interaction")
}