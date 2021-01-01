package at.shockbytes.dante.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.core.image.picker.ImagePicking
import at.shockbytes.dante.core.login.AuthenticationSource
import at.shockbytes.dante.core.login.AuthenticationSource.ANONYMOUS
import at.shockbytes.dante.core.login.DanteUser
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.core.user.UserRepository
import at.shockbytes.dante.storage.ImageUploadStorage
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
import timber.log.Timber
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
    private val themeRepository: ThemeRepository,
    private val imagePicker: ImagePicking,
    private val imageUploadStorage: ImageUploadStorage,
    private val userRepository: UserRepository
) : BaseViewModel() {

    sealed class UserEvent {

        data class LoggedIn(
            val user: DanteUser,
            val profileActionViewState: ProfileActionViewState
        ) : UserEvent()

        object UnauthenticatedUser : UserEvent()
    }

    sealed class MainEvent {

        object Announcement : MainEvent()

        object Login : MainEvent()

        object AnonymousLogout : MainEvent()

        data class AnonymousUpgradeFailed(val message: String?) : MainEvent()
    }

    private val userEvent = MutableLiveData<UserEvent>()
    fun getUserEvent(): LiveData<UserEvent> = userEvent

    private val eventSubject = PublishSubject.create<MainEvent>()
    fun onMainEvent(): Observable<MainEvent> = eventSubject

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
            ANONYMOUS -> ProfileActionViewState.forAnonymousUser()
            else -> ProfileActionViewState.Hidden
        }
    }

    fun forceLogin(source: LoginSource) {
        postLoginEventAndTrackValue(source)
    }

    fun forceLogout() {
        loginRepository.logout()
            .subscribe({
                Timber.d("Successfully forced to logout user")
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun loginLogout() {
        loginRepository.getAccount()
            .subscribeOn(schedulers.io)
            .doOnError {
                userEvent.postValue(UserEvent.UnauthenticatedUser)
            }
            .flatMapCompletable(::mapUserStateToLoginAction)
            .subscribe({ }, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun mapUserStateToLoginAction(userState: UserState): Completable =
        when (userState) {
            is UserState.SignedInUser -> {
                if (userState.isAnonymousLogout()) {
                    postAnonymousLogoutEvent()
                } else {
                    loginRepository.logout()
                }
            }
            is UserState.Unauthenticated -> postSignInEvent()
        }

    private fun UserState.isAnonymousLogout(): Boolean {
        return this is UserState.SignedInUser && user.authenticationSource == ANONYMOUS
    }

    private fun postAnonymousLogoutEvent(): Completable {
        return completableOf {
            eventSubject.onNext(MainEvent.AnonymousLogout)
        }
    }

    private fun postSignInEvent(): Completable {
        return completableOf {
            postLoginEventAndTrackValue(LoginSource.FromMenu)
        }
    }

    private fun postLoginEventAndTrackValue(source: LoginSource) {
        tracker.track(DanteTrackingEvent.Login(source))
        eventSubject.onNext(MainEvent.Login)
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
            eventSubject.onNext(MainEvent.Announcement)
        }
    }

    fun anonymousUpgrade(credentials: MailLoginCredentials) {
        loginRepository.upgradeAnonymousAccount(credentials.address, credentials.password)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                // TODO Post event
                Timber.e("Successfully upgrade anonymous account")
            }, { throwable ->
                eventSubject.onNext(MainEvent.AnonymousUpgradeFailed(throwable.localizedMessage))
            })
            .addTo(compositeDisposable)
    }

    fun changeUserImage(activity: FragmentActivity) {
        imagePicker
            .openGallery(activity)
            .flatMap(imageUploadStorage::uploadUserImage)
            .flatMapCompletable(userRepository::updateUserImage)
            .doOnComplete(loginRepository::reloadAccount)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                // TODO Post event
            }, {
                // TODO Post event
            })
            .addTo(compositeDisposable)
    }

    fun changeUserName(userName: String) {

        if (!verifyUserName(userName)) {
            // TODO Post event
            return
        }

        userRepository.updateUserName(userName)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .doOnComplete(loginRepository::reloadAccount)
            .subscribe({
                // TODO Post event
            }, {
                // TODO Post event
            })
            .addTo(compositeDisposable)
    }

    private fun verifyUserName(userName: String): Boolean {
        // TODO
        return true
    }

    fun updatePassword(password: String) {

    }
}