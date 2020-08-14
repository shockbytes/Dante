package at.shockbytes.tracking.event

sealed class DanteTrackingEvent(
    val name: String,
    val props: List<TrackingProperty> = listOf()
) {

    class BackupMadeEvent(backupProvider: String) : DanteTrackingEvent(
        "backup_made",
        listOf(TrackingProperty("backup_provider", backupProvider))
    )

    class InterestedInOnlineStorageEvent : DanteTrackingEvent(
        "interested_in_online_storage",
        listOf()
    )

    class StartImport(name: String) : DanteTrackingEvent(
        "start_import",
        listOf(TrackingProperty("importer_name", name))
    )

    class BurnDownLibrary : DanteTrackingEvent(
        "burn_down_library",
        listOf()
    )

    class TrackingStateChanged(state: Boolean) : DanteTrackingEvent(
        "tracking_state_changed",
        listOf(TrackingProperty("state", state))
    )

    data class PickRandomBook(val booksInBacklog: Int): DanteTrackingEvent(
        "pick_random_book",
        listOf(TrackingProperty("backlog_count", booksInBacklog))
    )

    object DisableRandomBookInteraction : DanteTrackingEvent("disable_random_book_interaction")
}