package at.shockbytes.dante.onboarding

sealed class OnboardingStep {

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