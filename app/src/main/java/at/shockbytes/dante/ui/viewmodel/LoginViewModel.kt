package at.shockbytes.dante.ui.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.singleOf
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

    fun authorizeWithMail(address: String, password: String, isSignUp: Boolean) {
        if (isSignUp) {
            login(loginRepository.createAccountWithMail(address, password))
        } else {
            login(loginRepository.loginWithMail(address, password))
        }
    }

    fun loginAnonymously() {
        login(loginRepository.loginAnonymously())
    }

    fun loginWithGoogle(data: Intent) {
        login(loginRepository.loginWithGoogle(data))
    }

    private fun login(source: Completable) {
        source
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                loginState.postValue(LoginState.LoggedIn)
            }, {
                loginState.postValue(LoginState.Error(R.string.login_error_google))
            })
            .addTo(compositeDisposable)
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        data class Error(@StringRes val errorRes: Int) : LoginState()
    }
}