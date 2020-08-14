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

    object BurnDownLibrary : DanteTrackingEvent("burn_down_library")

    data class TrackingStateChanged(val state: Boolean) : DanteTrackingEvent(
        "tracking_state_changed",
        listOf(TrackingProperty("state", state))
    )

    data class PickRandomBook(val booksInBacklog: Int): DanteTrackingEvent(
        "pick_random_book",
        listOf(TrackingProperty("backlog_count", booksInBacklog))
    )

    object DisableRandomBookInteraction : DanteTrackingEvent("disable_random_book_interaction")
}