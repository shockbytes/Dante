package at.shockbytes.dante.onboarding

sealed class OnboardingStepAction {

    // Initial view, no interaction here
    object Welcome : OnboardingStepAction()

    data class Tracking(val enableTracking: Boolean = false) : OnboardingStepAction()

    data class NightMode(val enableNightMode: Boolean = false) : OnboardingStepAction()

    data class Login(val loginMethod: LoginMethod) : OnboardingStepAction()

    // This one is disable for now as long as there are no suggestions
    data class Suggestions(val enableSuggestions: Boolean = false) : OnboardingStepAction()

    /**
     * @param openCamera If true, MainActivity will directly head into the camera view
     */
    data class CallToAction(val openCamera: Boolean = false) : OnboardingStepAction()
}