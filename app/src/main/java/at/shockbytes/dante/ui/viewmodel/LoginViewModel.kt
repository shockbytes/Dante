package at.shockbytes.dante.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.singleOf
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val danteSettings: DanteSettings,
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

    fun login() {
        danteSettings.lastLogin = System.currentTimeMillis()
        // Login user here...
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
            .subscribe({
               Timber.d("Logged in")
                // userEvent.postValue(MainViewModel.UserEvent.LoggedIn(account))
            }, { throwable: Throwable ->
                Timber.e(throwable)
                // userEvent.postValue(MainViewModel.UserEvent.Error(R.string.error_google_login))
            })
            .addTo(compositeDisposable)
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()
    }
}