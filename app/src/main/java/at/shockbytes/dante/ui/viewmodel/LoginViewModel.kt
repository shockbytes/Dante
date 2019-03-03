package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import javax.inject.Inject

class LoginViewModel @Inject constructor() : BaseViewModel() {

    private val nextOnboardingStep = MutableLiveData<OnboardingStep>()
    fun getNextOnboardingStep(): LiveData<OnboardingStep> = nextOnboardingStep

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

    // TODO Move in appropriate class
    sealed class OnboardingStep {

        enum class LoginMethod {
            GOOGLE
        }

        // Initial view, no interaction here
        object Welcome : OnboardingStep()

        data class Tracking(val enableTracking: Boolean) : OnboardingStep()

        data class NightMode(val enableNightMode: Boolean) : OnboardingStep()

        data class Login(val loginMethod: LoginMethod) : OnboardingStep()

        // This one is disable for now as long as there are no suggestions
        data class Suggestions(val enableSuggestions: Boolean) : OnboardingStep()

        /**
         * @param openCamera If true, MainActivity will directly head into the camera view
         */
        data class CallToAction(val openCamera: Boolean) : OnboardingStep()
    }
}