package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.onboarding.LoginMethod
import at.shockbytes.dante.onboarding.OnboardingStepAction
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.log

class LoginViewModel @Inject constructor(
        private val danteSettings: DanteSettings,
        private val signInManager: SignInManager,
        private val schedulers: SchedulerFacade
) : BaseViewModel() {

    private val nextOnboardingStep = MutableLiveData<OnboardingStepAction>()
    fun getNextOnboardingStep(): LiveData<OnboardingStepAction> = nextOnboardingStep

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    fun requestLoginState() {

        signInManager.isSignedIn()
                .subscribeOn(schedulers.io)
                .map { isLoggedIn ->
                    when {
                        isLoggedIn -> LoginState.LoggedIn
                        danteSettings.lastLogin != 0L -> LoginState.LoggedOut
                        // Never logged in
                        else -> LoginState.FirstAppOpen
                    }
                }
                .subscribe({ state ->
                    loginState.postValue(state)
                }, { throwable ->
                    Timber.e(throwable)
                    loginState.postValue(LoginState.LoggedOut)
                })
                .addTo(compositeDisposable)
    }

    fun login() {
        danteSettings.lastLogin = System.currentTimeMillis()
        // TODO log the user in
    }

    fun nextOnboardingStep(currentStep: OnboardingStepAction) {
        when (currentStep) {
            is OnboardingStepAction.Welcome -> {
                nextOnboardingStep.postValue(OnboardingStepAction.NightMode())
            }
            is OnboardingStepAction.NightMode -> {
                nextOnboardingStep.postValue(OnboardingStepAction.Tracking())
                danteSettings.darkModeEnabled = currentStep.enableNightMode
            }
            is OnboardingStepAction.Tracking -> {
                nextOnboardingStep.postValue(OnboardingStepAction.Login(LoginMethod.GOOGLE))
                danteSettings.trackingEnabled = currentStep.enableTracking
            }
            is OnboardingStepAction.Login -> {
                nextOnboardingStep.postValue(OnboardingStepAction.Suggestions())
            }
            is OnboardingStepAction.Suggestions -> {
                nextOnboardingStep.postValue(OnboardingStepAction.CallToAction())
            }
            is OnboardingStepAction.CallToAction -> {
                // nextOnboardingStep.postValue(OnboardingStepAction.)
            }
        }
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        object FirstAppOpen : LoginState()
    }
}