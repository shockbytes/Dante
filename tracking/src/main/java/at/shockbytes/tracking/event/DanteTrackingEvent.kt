package at.shockbytes.tracking.event

import at.shockbytes.tracking.properties.BaseProperty
import at.shockbytes.tracking.properties.LoginSource

sealed class DanteTrackingEvent(
    val name: String,
    val props: List<BaseProperty<Any>> = listOf()
) {

    // Login
    object OpenTermsOfServices : DanteTrackingEvent("open_terms_of_services", listOf())

    data class BackupMadeEvent(val backupProvider: String) : DanteTrackingEvent(
        "backup_made",
        listOf(TrackingProperty("backup_provider", backupProvider))
    )

    object InterestedInOnlineStorageEvent : DanteTrackingEvent("interested_in_online_storage")

    object OpenAdFreeMediumArticle : DanteTrackingEvent("open_ad_free_medium_article")

    data class StartImport(val importer: String) : DanteTrackingEvent(
        "start_import",
        listOf(TrackingProperty("importer_name", importer))
    )

    data class Login(val source: LoginSource) : DanteTrackingEvent(
        "login",
        listOf(source)
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

    data class SuggestBook(val title: String) : DanteTrackingEvent(
        "suggest_book",
        listOf(
            TrackingProperty("book_title", title)
        )
    )

    object DisableRandomBookInteraction : DanteTrackingEvent("disable_random_book_interaction")
}