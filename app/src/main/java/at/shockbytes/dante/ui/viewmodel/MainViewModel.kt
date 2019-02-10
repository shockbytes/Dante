package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import at.shockbytes.dante.R
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(private val signInManager: SignInManager) : BaseViewModel() {

    sealed class UserEvent {
        data class SuccessEvent(val user: DanteUser?, val showWelcomeScreen: Boolean) : UserEvent()
        class LoginEvent(val signInIntent: Intent?) : UserEvent()
        data class ErrorEvent(val errorMsg: Int) : UserEvent()
    }

    val userEvent = MutableLiveData<UserEvent>()

    init {
        initialize()
    }

    private fun initialize() {
        signInManager.setup()
        signInManager.isSignedIn().subscribe { isSignedIn ->

            if (isSignedIn) { // <- User signed in, TOP!
                userEvent.postValue(UserEvent.SuccessEvent(
                        signInManager.getAccount(), signInManager.showWelcomeScreen))
            } else if (!isSignedIn) { // <- User not signed in, reset UI
                userEvent.postValue(UserEvent.SuccessEvent(null, signInManager.showWelcomeScreen))
            }

            // User not signed in and did not opt-out for login screen
            if (!isSignedIn && !signInManager.maybeLater) {
                userEvent.postValue(UserEvent.LoginEvent(signInManager.signInIntent))
            }
        }
        .addTo(compositeDisposable)
    }

    fun signIn(data: Intent, signInToBackend: Boolean) {
        signInManager.signIn(data, signInToBackend).subscribe({ account ->
            userEvent.postValue(UserEvent.SuccessEvent(account, signInManager.showWelcomeScreen))
        }, { throwable: Throwable ->
            Timber.e(throwable)
            userEvent.postValue(UserEvent.ErrorEvent(R.string.error_google_login))
        }).addTo(compositeDisposable)
    }

    fun loginLogout() {

        if (signInManager.getAccount() != null) {
            signInManager.signOut().subscribe { }.addTo(compositeDisposable)
        } else {
            userEvent.postValue(UserEvent.LoginEvent(signInManager.signInIntent))
        }
    }

    fun signInMaybeLater(maybeLater: Boolean) {
        signInManager.maybeLater = maybeLater
    }

    fun showSignInWelcomeScreen(showWelcomeScreen: Boolean) {
        signInManager.showWelcomeScreen = showWelcomeScreen
    }
}