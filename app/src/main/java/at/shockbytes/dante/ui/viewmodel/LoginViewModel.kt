package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.onboarding.OnboardingStep
import javax.inject.Inject

class LoginViewModel @Inject constructor() : BaseViewModel() {

    private val nextOnboardingStep = MutableLiveData<OnboardingStep>()
    fun getNextOnboardingStep(): LiveData<OnboardingStep> = nextOnboardingStep

    private val loginState = MutableLiveData<LoginState>()
    fun getLoginState(): LiveData<LoginState> = loginState

    fun requestLoginState() {
        loginState.postValue(LoginState.FirstAppOpen)
    }

    fun login() {
        // TODO log the user in
    }

    fun nextOnboardingStep(currentStep: OnboardingStep) {
        when (currentStep) {
            is OnboardingStep.Welcome -> {
            }
            is OnboardingStep.NightMode -> {
            }
            is OnboardingStep.Tracking -> {
            }
            is OnboardingStep.Login -> {
            }
            is OnboardingStep.Suggestions -> {
            }
            is OnboardingStep.CallToAction -> {
            }
        }
    }

    sealed class LoginState {

        object LoggedIn : LoginState()

        object LoggedOut : LoginState()

        object FirstAppOpen : LoginState()
    }
}