package at.shockbytes.dante.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.SignInManager
import com.crashlytics.android.Crashlytics
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 10-Jun-18.
 */
class MainViewModel @Inject constructor(private val signInManager: SignInManager,
                                        app: Application): AndroidViewModel(app) {

    private val compositeDisposable = CompositeDisposable()

    sealed class UserEvent {
        data class SuccessEvent(val user: DanteUser?): UserEvent()
        class LoginEvent: UserEvent()
        data class ErrorEvent(val errorMsg: Int): UserEvent()
    }


    val userEvent = MutableLiveData<UserEvent>()

    init {
        poke()
    }

    private fun poke() {
        signInManager.setup(getApplication())
        compositeDisposable.add(signInManager.isSignedIn(getApplication()).subscribe { isSignedIn ->

            if (isSignedIn) { // <- User signed in, TOP!
                userEvent.postValue(UserEvent.SuccessEvent(signInManager.getAccount(getApplication())))
            } else if (!signInManager.maybeLater) { // <- userEvent not signed in and did not opt-out
                userEvent.postValue(UserEvent.LoginEvent())
            }
        })
    }

    fun signIn(data: Intent) {

        signInManager.signIn(data).subscribe({ account ->
            userEvent.postValue(UserEvent.SuccessEvent(account))
        }, { throwable: Throwable ->
            throwable.printStackTrace()
            Crashlytics.logException(throwable)
            userEvent.postValue(UserEvent.ErrorEvent(R.string.error_google_login))
        })

    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}

