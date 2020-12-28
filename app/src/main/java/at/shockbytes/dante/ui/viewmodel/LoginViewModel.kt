package at.shockbytes.dante.ui.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.singleOf
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    init {
        resolveLoginState()
    }

    private fun resolveLoginState() {
        loginRepository.getAccount()
            .map { userState ->
                when (userState) {
                    is UserState.SignedInUser -> LoginState.LoggedIn
                    UserState.Unauthenticated -> LoginState.LoggedOut
                }
            }
            .doOnError { loginState.postValue(LoginState.LoggedOut) }
            .subscribe(loginState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun requestGoogleLogin(): Single<Intent> {
        return singleOf {
            loginRepository.googleLoginIntent
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
            .subscribe({
                loginState.postValue(LoginState.LoggedIn)
            }, { throwable ->
                handleLoginErrorState(throwable, errorMessageRes)
            })
            .addTo(compositeDisposable)
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

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        data class Error(@StringRes val errorMessageRes: Int) : LoginState()
    }
}