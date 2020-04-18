package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.util.settings.DanteSettings
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val danteSettings: DanteSettings
) : BaseViewModel() {

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    fun requestLoginState() {
        loginState.postValue(LoginState.FirstAppOpen)
    }

    fun login() {
        danteSettings.lastLogin = System.currentTimeMillis()
        // TODO log the user in
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        object FirstAppOpen : LoginState()
    }
}