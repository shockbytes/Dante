package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.signin.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(
    private val signInManager: SignInManager,
    private val announcementProvider: AnnouncementProvider,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    sealed class UserEvent {

        data class SuccessEvent(val user: DanteUser?, val showWelcomeScreen: Boolean) : UserEvent()

        data class LoginEvent(val signInIntent: Intent?) : UserEvent()

        data class ErrorEvent(@StringRes val errorMsg: Int) : UserEvent()
    }

    private val userEvent = MutableLiveData<UserEvent>()
    fun getUserEvent(): LiveData<UserEvent> = userEvent

    private val activeAnnouncement = PublishSubject.create<Boolean>()
    fun hasActiveAnnouncement(): Observable<Boolean> = activeAnnouncement

    init {
        initialize()
    }

    private fun initialize() {
        signInManager.setup()
        signInManager.observeSignInState()
            .map { userState ->
                when {
                    userState is UserState.SignedInUser -> {
                        UserEvent.SuccessEvent(userState.user, signInManager.showWelcomeScreen)
                    }
                    userState is UserState.AnonymousUser && !signInManager.maybeLater -> {
                        UserEvent.LoginEvent(signInManager.signInIntent)
                    }
                    else -> UserEvent.SuccessEvent(null, signInManager.showWelcomeScreen)
                }
            }
            .subscribe(userEvent::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun signIn(data: Intent) {
        signInManager.signIn(data)
            .subscribe({ account ->
                userEvent.postValue(UserEvent.SuccessEvent(account, signInManager.showWelcomeScreen))
            }, { throwable: Throwable ->
                Timber.e(throwable)
                userEvent.postValue(UserEvent.ErrorEvent(R.string.error_google_login))
            })
            .addTo(compositeDisposable)
    }

    fun loginLogout() {
        signInManager.getAccount()
            .subscribeOn(schedulers.io)
            .doOnError {
                userEvent.postValue(UserEvent.LoginEvent(signInManager.signInIntent))
            }
            .flatMapCompletable { userState ->
                when (userState) {
                    is UserState.SignedInUser -> signInManager.signOut()
                    UserState.AnonymousUser -> postSignInEvent()
                }
            }
            .subscribe({ }, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun postSignInEvent(): Completable {
        return Completable.fromAction {
            userEvent.postValue(UserEvent.LoginEvent(signInManager.signInIntent))
        }
    }

    fun signInMaybeLater(maybeLater: Boolean) {
        signInManager.maybeLater = maybeLater
    }

    fun showSignInWelcomeScreen(showWelcomeScreen: Boolean) {
        signInManager.showWelcomeScreen = showWelcomeScreen
    }

    fun queryAnnouncements() {
        val hasActiveAnnouncement = announcementProvider.getActiveAnnouncement() != null
        activeAnnouncement.onNext(hasActiveAnnouncement)
    }
}