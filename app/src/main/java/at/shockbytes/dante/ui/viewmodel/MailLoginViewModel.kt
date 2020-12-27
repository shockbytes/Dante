package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.util.ExceptionHandlers
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
        data class PasswordVerification(val isSignUp: Boolean) : MailLoginStep()
    }

    private var mailAddress: CharSequence = ""
    private var password: CharSequence = ""
    private var isSignUp: Boolean = false

    private val step = MutableLiveData<MailLoginStep>()
    fun getStep(): LiveData<MailLoginStep> = step

    private val isMailValid = BehaviorSubject.create<Boolean>()
    fun isMailValid(): Observable<Boolean> = isMailValid

    private val isPasswordValid = BehaviorSubject.create<Boolean>()
    fun isPasswordValid(): Observable<Boolean> = isPasswordValid

    fun initialize(state: MailLoginState) {
        val currentStep = when (state) {
            is MailLoginState.ResolveEmailAddress -> MailLoginStep.MailVerification
            is MailLoginState.ShowEmailAndPassword -> MailLoginStep.PasswordVerification(state.isSignUp)
        }
        step.postValue(currentStep)
    }

    fun verifyMailAddress(mailAddress: CharSequence) {
        this.mailAddress = mailAddress
        isMailValid.onNext(MailValidator.validateMail(mailAddress))
    }

    fun verifyPassword(password: CharSequence) {
        this.password = password
        isPasswordValid.onNext(validatePassword(password))
    }

    private fun validatePassword(password: CharSequence): Boolean {
        return password.count() >= MINIMUM_PASSWORD_LENGTH
    }

    fun checkIfAccountExistsForMailAddress() {
        loginRepository.fetchSignInMethodsForEmail(mailAddress.toString())
            .map { methods ->
                Timber.d(methods.toString())
                // Save isSignUp as a side effect which will be later passed to parent fragment
                isSignUp = !methods.contains("mail") // TODO Check this string
                MailLoginStep.PasswordVerification(isSignUp)
            }
            .subscribe(step::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    sealed class MailLoginState {

        object ResolveEmailAddress : MailLoginState()
        data class ShowEmailAndPassword(val isSignUp: Boolean) : MailLoginState()
    }

    companion object {

        private const val MINIMUM_PASSWORD_LENGTH = 6
    }
}
