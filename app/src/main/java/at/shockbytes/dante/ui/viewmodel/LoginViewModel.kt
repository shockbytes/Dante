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

    fun loginWithMail(address: String, password: String) {
        // TODO
    }

    fun anonymousLogin() {
        // TODO
    }

    fun requestGoogleLogin(): Single<Intent> {
        return singleOf {
            loginRepository.googleLoginIntent
        }
    }

    fun loginWithGoogle(data: Intent) {
        loginRepository.loginWithGoogle(data)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                loginState.postValue(LoginState.LoggedIn)
            }, {
                loginState.postValue(LoginState.Error(R.string.error_google_login))
            })
            .addTo(compositeDisposable)
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        data class Error(@StringRes val errorRes: Int) : LoginState()
    }
}