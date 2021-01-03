package at.shockbytes.dante.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.image.picker.ImagePicking
import at.shockbytes.dante.core.login.AuthenticationSource
import at.shockbytes.dante.core.login.DanteUser
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.core.user.UserRepository
import at.shockbytes.dante.storage.ImageUploadStorage
import at.shockbytes.dante.ui.custom.profile.ProfileActionViewState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import at.shockbytes.tracking.properties.LoginSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val imagePicker: ImagePicking,
    private val imageUploadStorage: ImageUploadStorage,
    private val userRepository: UserRepository,
    private val schedulers: SchedulerFacade,
    private val tracker: Tracker,
): BaseViewModel() {


    sealed class UserViewState {

        data class LoggedIn(
            val user: DanteUser,
            val profileActionViewState: ProfileActionViewState
        ) : UserViewState()

        object UnauthenticatedUser : UserViewState()
    }
    
    sealed class UserEvent {

        object Login : UserEvent()

        object AnonymousLogout : UserEvent()

        data class AnonymousUpgradeFailed(val message: String?) : UserEvent()

        sealed class UserNameEvent : UserEvent() {

            object UserNameUpdated : UserNameEvent()

            data class UserNameUpdateError(val message: String?) : UserNameEvent()

            object UserNameEmpty : UserNameEvent()

            data class UserNameTooLong(
                val currentLength: Int,
                val maxAllowedLength: Int
            ): UserNameEvent()
        }

        sealed class UserImageEvent : UserEvent() {

            object UserImageUpdated : UserImageEvent()

            data class UserImageUpdateError(val message: String?) : UserImageEvent()
        }
    }

    private val userViewState = MutableLiveData<UserViewState>()
    fun getUserViewState(): LiveData<UserViewState> = userViewState

    private val userEventSubject = PublishSubject.create<UserEvent>()
    fun onUserEvent(): Observable<UserEvent> = userEventSubject

    init {
        initialize()
    }

    private fun initialize() {
        loginRepository.observeAccount()
            .map(::mapUserStateToUserEvent)
            .subscribe(userViewState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun mapUserStateToUserEvent(userState: UserState) = when (userState) {
        is UserState.SignedInUser -> {
            UserViewState.LoggedIn(userState.user, resolveProfileActionViewState(userState.user))
        }
        is UserState.Unauthenticated -> {
            UserViewState.UnauthenticatedUser
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
                userViewState.postValue(UserViewState.UnauthenticatedUser)
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
        return this is UserState.SignedInUser && user.authenticationSource == AuthenticationSource.ANONYMOUS
    }

    private fun postAnonymousLogoutEvent(): Completable {
        return completableOf {
            userEventSubject.onNext(UserEvent.AnonymousLogout)
        }
    }

    private fun postSignInEvent(): Completable {
        return completableOf {
            postLoginEventAndTrackValue(LoginSource.FromMenu)
        }
    }

    private fun postLoginEventAndTrackValue(source: LoginSource) {
        tracker.track(DanteTrackingEvent.Login(source))
        userEventSubject.onNext(UserEvent.Login)
    }

    fun anonymousUpgrade(credentials: MailLoginCredentials) {
        loginRepository.upgradeAnonymousAccount(credentials.address, credentials.password)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                // TODO Post event
                Timber.e("Successfully upgrade anonymous account")
            }, { throwable ->
                userEventSubject.onNext(UserEvent.AnonymousUpgradeFailed(throwable.localizedMessage))
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
                userEventSubject.onNext(UserEvent.UserImageEvent.UserImageUpdated)
            }, { throwable ->
                userEventSubject.onNext(
                    UserEvent.UserImageEvent.UserImageUpdateError(throwable.localizedMessage)
                )
            })
            .addTo(compositeDisposable)
    }

    fun changeUserName(userName: String) {

        if (!verifyUserName(userName)) {
            postUserNameErrorEvent(userName)
            return
        }

        userRepository.updateUserName(userName)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .doOnComplete(loginRepository::reloadAccount)
            .subscribe({
                userEventSubject.onNext(UserEvent.UserNameEvent.UserNameUpdated)
            }, { throwable ->
                userEventSubject.onNext(
                    UserEvent.UserNameEvent.UserNameUpdateError(throwable.localizedMessage)
                )
            })
            .addTo(compositeDisposable)
    }

    private fun verifyUserName(userName: String): Boolean {
        return userName.length in 1 until MAX_ALLOWED_USERNAME_LENGTH
    }

    private fun postUserNameErrorEvent(userName: String) {
        val event = if (userName.isEmpty()) {
            UserEvent.UserNameEvent.UserNameEmpty
        } else {
            UserEvent.UserNameEvent.UserNameTooLong(
                currentLength = userName.length,
                maxAllowedLength = MAX_ALLOWED_USERNAME_LENGTH
            )
        }

        userEventSubject.onNext(event)
    }

    fun updatePassword(password: String) {
        // TODO Done in another PR
    }

    companion object {

        private const val MAX_ALLOWED_USERNAME_LENGTH = 32
    }
}