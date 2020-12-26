package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.SignInRepository
import at.shockbytes.dante.signin.UserState
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.theme.ThemeRepository
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
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(
    private val signInRepository: SignInRepository,
    private val announcementProvider: AnnouncementProvider,
    private val schedulers: SchedulerFacade,
    private val danteSettings: DanteSettings,
    private val tracker: Tracker,
    private val themeRepository: ThemeRepository
) : BaseViewModel() {

    sealed class UserEvent {

        data class LoggedIn(val user: DanteUser) : UserEvent()

        object AnonymousUser : UserEvent()

        data class RequireLogin(val signInIntent: Intent?) : UserEvent()

        data class Error(@StringRes val errorMsg: Int) : UserEvent()
    }

    private val userEvent = MutableLiveData<UserEvent>()
    fun getUserEvent(): LiveData<UserEvent> = userEvent

    private val showAnnouncementSubject = PublishSubject.create<Unit>()
    fun showAnnouncement(): Observable<Unit> = showAnnouncementSubject

    private val seasonalThemeSubject = BehaviorSubject.create<SeasonalTheme>()
    fun getSeasonalTheme(): Observable<SeasonalTheme> = seasonalThemeSubject
        .delay(2, TimeUnit.SECONDS)
        .distinctUntilChanged()

    init {
        initialize()
    }

    private fun initialize() {
        signInRepository.setup()
        signInRepository.observeSignInState()
            .map(::mapUserStateToUserEvent)
            .subscribe(userEvent::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun mapUserStateToUserEvent(userState: UserState) = when (userState) {
        is UserState.SignedInUser -> {
            UserEvent.LoggedIn(userState.user)
        }
        is UserState.Unauthenticated -> {
            UserEvent.RequireLogin(signInRepository.signInIntent)
        }
    }

    fun forceLogin(source: LoginSource) {
        postSignInEventAndTrackValue(source)
    }

    fun signIn(data: Intent) {
        signInRepository.signIn(data)
            .subscribe({ account ->
                userEvent.postValue(UserEvent.LoggedIn(account))
            }, { throwable: Throwable ->
                Timber.e(throwable)
                userEvent.postValue(UserEvent.Error(R.string.error_google_login))
            })
            .addTo(compositeDisposable)
    }

    fun loginLogout() {
        signInRepository.getAccount()
            .subscribeOn(schedulers.io)
            .doOnError {
                userEvent.postValue(UserEvent.RequireLogin(signInRepository.signInIntent))
            }
            .flatMapCompletable { userState ->
                when (userState) {
                    is UserState.SignedInUser -> signInRepository.signOut()
                    UserState.Unauthenticated -> postSignInEvent()
                }
            }
            .subscribe({ }, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun postSignInEvent(): Completable {
        return completableOf {
            postSignInEventAndTrackValue(LoginSource.FromMenu)
        }
    }

    private fun postSignInEventAndTrackValue(source: LoginSource) {
        tracker.track(DanteTrackingEvent.Login(source))
        userEvent.postValue(UserEvent.RequireLogin(signInRepository.signInIntent))
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