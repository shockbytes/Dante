package at.shockbytes.dante.ui.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.AuthenticationSource
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.MailValidator
import at.shockbytes.dante.util.addTo
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
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
    private val loginRepository: LoginRepository,
    private val tracker: Tracker
) : BaseViewModel() {

    sealed class MailLoginStep {
        object MailVerification : MailLoginStep()
        data class PasswordVerification(
            val isSignUp: Boolean,
            val textHeader: Int,
            val isEmailEnabled: Boolean,
            val focusOnPasswordField: Boolean
        ) : MailLoginStep()
    }
    
    sealed class MailResetAction {

        abstract val mailAddress: CharSequence

        data class Success(override val mailAddress: CharSequence): MailResetAction()

        data class Error(override val mailAddress: CharSequence): MailResetAction()
    }

    private val mailResetAction = PublishSubject.create<MailResetAction>()
    fun getMailResetAction(): Observable<MailResetAction> = mailResetAction

    private val googleMailLoginAttemptSubject = PublishSubject.create<Unit>()
    fun onGoogleMailLoginAttempt(): Observable<Unit> = googleMailLoginAttemptSubject

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
            is MailLoginState.ResolveEmailAddress -> {
                MailLoginStep.MailVerification
            }
            is MailLoginState.ShowEmailAndPassword -> {
                // Set this as a side effect
                this.isSignUp = state.isSignUp
                MailLoginStep.PasswordVerification(
                    state.isSignUp,
                    state.textHeader,
                    isEmailEnabled = true,
                    focusOnPasswordField = false
                )
            }
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
        loginRepository.fetchRegisteredAuthenticationSourcesForEmail(mailAddress.toString())
            .map { methods ->

                if (methods.contains(AuthenticationSource.GOOGLE)) {
                    MailLoginStep.MailVerification
                } else {
                    // Save isSignUp as a side effect which will be later passed to parent fragment
                    isSignUp = !methods.contains(AuthenticationSource.MAIL)
                    MailLoginStep.PasswordVerification(
                        isSignUp,
                        R.string.login_mail_enter_password,
                        isEmailEnabled = false,
                        focusOnPasswordField = true
                    )
                }
            }
            .doOnSuccess {
                if (it is MailLoginStep.MailVerification) {
                    googleMailLoginAttemptSubject.onNext(Unit)
                }
            }
            .subscribe(step::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun getMailLoginCredentials(): MailLoginCredentials {
        return MailLoginCredentials(mailAddress.toString(), password.toString(), isSignUp)
    }

    fun userForgotPassword() {
        loginRepository.sendPasswordResetRequest(mailAddress.toString())
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                mailResetAction.onNext(MailResetAction.Success(mailAddress))
                tracker.track(DanteTrackingEvent.ResetPasswordSuccess)
            }, {
                mailResetAction.onNext(MailResetAction.Error(mailAddress))
                tracker.track(DanteTrackingEvent.ResetPasswordFailed)
            })
            .addTo(compositeDisposable)
    }

    sealed class MailLoginState : Parcelable {

        @Parcelize
        object ResolveEmailAddress : MailLoginState()

        @Parcelize
        data class ShowEmailAndPassword(
            val isSignUp: Boolean,
            val textHeader: Int
        ) : MailLoginState()
    }

    companion object {

        private const val MINIMUM_PASSWORD_LENGTH = 6
    }
}
