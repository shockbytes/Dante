package at.shockbytes.dante.ui.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.GoogleAuth
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.singleOf
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val googleAuth: GoogleAuth,
    private val tracker: Tracker,
    private val danteSettings: DanteSettings
) : BaseViewModel() {

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        data class Error(@StringRes val errorMessageRes: Int) : LoginState()
    }

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    sealed class LoginViewState {

        // User opens the app the first time
        object NewUser : LoginViewState()

        // User already signed in but signed out again
        object Standard : LoginViewState()

        companion object {

            fun of(isNewUser: Boolean): LoginViewState {
                return if (isNewUser) NewUser else Standard
            }
        }
    }

    private val loginViewState = MutableLiveData<LoginViewState>()
    fun getLoginViewState(): LiveData<LoginViewState> = loginViewState

    init {
        resolveLoginState()
    }

    private fun resolveLoginState() {
        loginRepository.getAccount()
            .map { userState ->
                when (userState) {
                    is UserState.SignedInUser -> LoginState.LoggedIn
                    is UserState.Unauthenticated -> LoginState.LoggedOut
                }
            }
            .doOnSuccess {
                // Only show this if the user did not sign in previously, ergo it's a new user
                loginViewState.postValue(LoginViewState.of(danteSettings.isNewUser))
            }
            .doOnError { loginState.postValue(LoginState.LoggedOut) }
            .subscribe(loginState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun requestGoogleLogin(): Single<Intent> {
        return singleOf {
            googleAuth.googleLoginIntent
        }
    }

    fun authorizeWithMail(credentials: MailLoginCredentials) {
        if (credentials.isSignUp) {
            login(loginRepository.createAccountWithMail(credentials.address, credentials.password))
        } else {
            login(loginRepository.loginWithMail(credentials.address, credentials.password))
        }
    }

    fun loginAnonymously() {
        login(loginRepository.loginAnonymously())
    }

    fun loginWithGoogle(data: Intent) {
        login(loginRepository.loginWithGoogle(data), errorMessageRes = R.string.login_error_google)
    }

    private fun login(
        source: Completable,
        @StringRes errorMessageRes: Int = R.string.login_general_error
    ) {
        source
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .doOnComplete(::markUserOpenedApp)
            .subscribe({
                loginState.postValue(LoginState.LoggedIn)
            }, { throwable ->
                handleLoginErrorState(throwable, errorMessageRes)
            })
            .addTo(compositeDisposable)
    }

    private fun markUserOpenedApp() {
        danteSettings.isNewUser = false
    }

    private fun handleLoginErrorState(throwable: Throwable, @StringRes errorMessageRes: Int) {
        val state = when (throwable) {
            is FirebaseAuthInvalidCredentialsException -> {
                LoginState.Error(R.string.login_invalid_credentials)
            }
            else -> {
                LoginState.Error(errorMessageRes)
            }
        }
        loginState.postValue(state)
    }

    fun trackOpenTermsOfServices() {
        tracker.track(DanteTrackingEvent.OpenTermsOfServices)
    }

    fun trackLoginProblemClicked() {
        TODO("Not yet implemented")
    }
}