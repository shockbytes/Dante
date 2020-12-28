package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.core.login.AuthenticationSource
import at.shockbytes.dante.core.login.DanteUser
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.theme.ThemeRepository
import at.shockbytes.dante.ui.custom.profile.ProfileActionViewState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import at.shockbytes.tracking.properties.LoginSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val announcementProvider: AnnouncementProvider,
    private val schedulers: SchedulerFacade,
    private val danteSettings: DanteSettings,
    private val tracker: Tracker,
    private val themeRepository: ThemeRepository
) : BaseViewModel() {

    sealed class UserEvent {

        data class LoggedIn(
            val user: DanteUser,
            val profileActionViewState: ProfileActionViewState
        ) : UserEvent()

        object UnauthenticatedUser : UserEvent()
    }

    private val userEvent = MutableLiveData<UserEvent>()
    fun getUserEvent(): LiveData<UserEvent> = userEvent

    private val showAnnouncementSubject = PublishSubject.create<Unit>()
    fun showAnnouncement(): Observable<Unit> = showAnnouncementSubject

    private val loginEvent = PublishSubject.create<Unit>()
    fun onLoginEvent(): Observable<Unit> = loginEvent

    private val seasonalThemeSubject = BehaviorSubject.create<SeasonalTheme>()
    fun getSeasonalTheme(): Observable<SeasonalTheme> = seasonalThemeSubject
        .delay(2, TimeUnit.SECONDS)
        .distinctUntilChanged()

    init {
        initialize()
    }

    private fun initialize() {
        loginRepository.observeAccount()
            .map(::mapUserStateToUserEvent)
            .subscribe(userEvent::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun mapUserStateToUserEvent(userState: UserState) = when (userState) {
        is UserState.SignedInUser -> {
            UserEvent.LoggedIn(userState.user, resolveProfileActionViewState(userState.user))
        }
        is UserState.Unauthenticated -> {
            UserEvent.UnauthenticatedUser
        }
    }

    private fun resolveProfileActionViewState(user: DanteUser): ProfileActionViewState {
        return when (user.authenticationSource) {
            AuthenticationSource.GOOGLE -> ProfileActionViewState.forGoogleUser()
            AuthenticationSource.MAIL -> ProfileActionViewState.forMailUser()
            AuthenticationSource.ANONYMOUS -> ProfileActionViewState.forAnonymousUser()
            else -> ProfileActionViewState.Hidden
        }
    }

    fun forceLogin(source: LoginSource) {
        postLoginEventAndTrackValue(source)
    }

    fun loginLogout() {
        loginRepository.getAccount()
            .subscribeOn(schedulers.io)
            .doOnError {
                userEvent.postValue(UserEvent.UnauthenticatedUser)
            }
            .flatMapCompletable { userState ->
                when (userState) {
                    is UserState.SignedInUser -> loginRepository.logout()
                    UserState.Unauthenticated -> postSignInEvent()
                }
            }
            .subscribe({ }, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun postSignInEvent(): Completable {
        return completableOf {
            postLoginEventAndTrackValue(LoginSource.FromMenu)
        }
    }

    private fun postLoginEventAndTrackValue(source: LoginSource) {
        tracker.track(DanteTrackingEvent.Login(source))
        loginEvent.onNext(Unit)
    }

    fun requestSeasonalTheme() {
        themeRepository.getSeasonalTheme()
            .doOnError { seasonalThemeSubject.onNext(SeasonalTheme.NoTheme) }
            .subscribe(seasonalThemeSubject::onNext, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun queryAnnouncements() {
        val hasActiveAnnouncement = announcementProvider.getActiveAnnouncement() != null
        // Do not show announcements if the user first logs into the app,
        // even though there would be a new announcement
        val showAnnouncement = hasActiveAnnouncement && !danteSettings.isFirstAppOpen
        if (showAnnouncement) {
            showAnnouncementSubject.onNext(Unit)
        }
    }
}