package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.signin.SignInRepository
import at.shockbytes.dante.signin.UserState
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.settings.DanteSettings
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val danteSettings: DanteSettings,
    private val signInRepository: SignInRepository
) : BaseViewModel() {

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    init {
        resolveLoginState()
    }

    private fun resolveLoginState() {
        signInRepository.getAccount()
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

    fun googleLogin() {
        // TODO
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()
    }
}