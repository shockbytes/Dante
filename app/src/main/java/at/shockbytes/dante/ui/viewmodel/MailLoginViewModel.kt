package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.util.MailValidator
import at.shockbytes.dante.util.addTo
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Date:    27.12.2020
 * Author:  Martin Macheiner
 *
 * Responsibilities:
 * 1. Validate mail
 * 2. Validate password length
 * 3. Check if mail is already in use
 */
class MailLoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    sealed class MailLoginStep {
        object MailVerification : MailLoginStep()
        object PasswordVerification : MailLoginStep()
    }

    private val step = MutableLiveData<MailLoginStep>()
    fun getStep(): LiveData<MailLoginStep> = step

    private val isMailValid = BehaviorSubject.create<Boolean>()
    fun isMailValid(): Observable<Boolean> = isMailValid

    private val isPasswordValid = BehaviorSubject.create<Boolean>()
    fun isPasswordValid(): Observable<Boolean> = isPasswordValid

    fun initialize(state: MailLoginState) {
        val currentStep = when (state) {
            MailLoginState.RESOLVE_EMAIL_ADDRESS -> MailLoginStep.MailVerification
            MailLoginState.SHOW_EMAIL_AND_PASSWORD -> MailLoginStep.PasswordVerification
        }
        step.postValue(currentStep)
    }

    fun verifyMailAddress(mailAddress: CharSequence) {
        isMailValid.onNext(MailValidator.validateMail(mailAddress))
    }

    fun verifyPassword(password: CharSequence) {
        isPasswordValid.onNext(validatePassword(password))
    }

    private fun validatePassword(password: CharSequence): Boolean {
        return password.count() >= MINIMUM_PASSWORD_LENGTH
    }

    fun checkIfAccountExistsForMailAddress(mailAddress: String) {
        loginRepository.fetchSignInMethodsForEmail(mailAddress)
            .subscribe({ methods ->
                Timber.d(methods.toString())
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    enum class MailLoginState {
        RESOLVE_EMAIL_ADDRESS, SHOW_EMAIL_AND_PASSWORD
    }

    companion object {

        private const val MINIMUM_PASSWORD_LENGTH = 6
    }
}
