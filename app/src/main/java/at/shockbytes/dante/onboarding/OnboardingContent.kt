package at.shockbytes.dante.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R

enum class OnboardingContent(
    @StringRes val contentRes: Int,
    @DrawableRes val headerIcon: Int,
    @RawRes val lottieRes: Int
) {

    WELCOME(R.string.ic_onboarding_welcome, R.drawable.ic_onboarding_welcome, R.raw.lottie_onboarding_welcome),

    NIGHT_MODE(R.string.ic_onboarding_night_mode, R.drawable.ic_onboarding_night_mode, R.raw.lottie_onboarding_dark_mode),

    SUGGESTIONS(R.string.ic_onboarding_suggestions, R.drawable.ic_onboarding_suggestions, R.raw.lottie_onboarding_suggestions),

    TRACKING(R.string.ic_onboarding_tracking, R.drawable.ic_onboarding_tracking, R.raw.lottie_onboarding_tracking),

    LOGIN(R.string.ic_onboarding_login, R.drawable.ic_onboarding_login, R.raw.lottie_onboarding_login),

    CALL_TO_ACTION(R.string.ic_onboarding_cta, R.drawable.ic_onboarding_cta, R.raw.lottie_onboarding_cta),
}