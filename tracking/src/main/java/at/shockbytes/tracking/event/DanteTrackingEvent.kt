package at.shockbytes.tracking.event

import at.shockbytes.dante.core.login.AuthenticationSource
import at.shockbytes.tracking.properties.BaseProperty
import at.shockbytes.tracking.properties.LoginSource

sealed class DanteTrackingEvent(
    val name: String,
    val props: List<BaseProperty<Any>> = listOf()
) {

    // -------------------------- Login & User --------------------------
    object OpenTermsOfServices : DanteTrackingEvent("open_terms_of_services", listOf())

    object ResetPasswordSuccess : DanteTrackingEvent("reset_password_success", listOf())

    object ResetPasswordFailed : DanteTrackingEvent("reset_password_failed", listOf())

    object ReportLoginProblem : DanteTrackingEvent("report_login_problem", listOf())

    data class OpenLogin(val source: LoginSource) : DanteTrackingEvent(
        "open_login",
        listOf(source)
    )

    data class Login(val source: AuthenticationSource): DanteTrackingEvent(
        "app_login",
        listOf(TrackingProperty("source", source.name))
    )

    data class SignUp(val source: AuthenticationSource): DanteTrackingEvent(
        "app_signup",
        listOf(TrackingProperty("source", source.name))
    )

    data class Logout(val source: AuthenticationSource): DanteTrackingEvent(
        "app_logout",
        listOf(TrackingProperty("source", source))
    )

    object AnonymousUpgrade : DanteTrackingEvent("anonymous_upgrade")

    // TODO Track this!
    object UpdateMailPassword: DanteTrackingEvent("update_mail_password")

    object UserNameChanged : DanteTrackingEvent("user_name_changed")

    object UserImageChanged : DanteTrackingEvent("user_image_changed")

    // ----------------------------- Storage ----------------------------

    data class BackupMadeEvent(val backupProvider: String) : DanteTrackingEvent(
        "backup_made",
        listOf(TrackingProperty("backup_provider", backupProvider))
    )

    object InterestedInOnlineStorageEvent : DanteTrackingEvent("interested_in_online_storage")

    data class StartImport(val importer: String) : DanteTrackingEvent(
        "start_import",
        listOf(TrackingProperty("importer_name", importer))
    )

    data class OpenBackupFile(val providerAcronym: String) : DanteTrackingEvent(
        "open_backup_file",
        listOf(TrackingProperty("backup_provider", providerAcronym))
    )

    data class TrackingStateChanged(val state: Boolean) : DanteTrackingEvent(
        "tracking_state_changed",
        listOf(TrackingProperty("state", state))
    )

    data class PickRandomBook(val booksInBacklog: Int) : DanteTrackingEvent(
        "pick_random_book",
        listOf(TrackingProperty("backlog_count", booksInBacklog))
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

    object OpenAdFreeMediumArticle : DanteTrackingEvent("open_ad_free_medium_article")

    object DisableRandomBookInteraction : DanteTrackingEvent("disable_random_book_interaction")
}