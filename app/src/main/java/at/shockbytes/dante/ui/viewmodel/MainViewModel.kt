package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.content.Intent
import androidx.lifecycle.LiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
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
        class LoginEvent(val signInIntent: Intent?) : UserEvent()
        data class ErrorEvent(val errorMsg: Int) : UserEvent()
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
            .subscribe { isSignedIn ->

                if (isSignedIn) { // <- User signed in, TOP!
                    userEvent.postValue(
                        UserEvent.SuccessEvent(
                            signInManager.getAccount().blockingGet(), // TODO rework this
                            signInManager.showWelcomeScreen
                        )
                    )
                } else { // <- User not signed in, reset UI
                    userEvent.postValue(UserEvent.SuccessEvent(null, signInManager.showWelcomeScreen))
                }

                // User not signed in and did not opt-out for login screen
                if (!isSignedIn && !signInManager.maybeLater) {
                    userEvent.postValue(UserEvent.LoginEvent(signInManager.signInIntent))
                }
            }
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
            .flatMapCompletable { signInManager.signOut() }
            .subscribe({
                Timber.d("Successfully logged out!")
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
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