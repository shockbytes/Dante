package at.shockbytes.dante.onboarding

import android.support.annotation.DrawableRes
import android.support.annotation.RawRes
import android.support.annotation.StringRes
import at.shockbytes.dante.R

enum class OnboardingContent(
    @StringRes val contentRes: Int,
    @DrawableRes val headerIcon: Int,
    @RawRes val lottieRes: Int? = null
) {

    WELCOME(R.string.ic_onboarding_welcome, R.drawable.ic_onboarding_welcome),

    NIGHT_MODE(R.string.ic_onboarding_night_mode, R.drawable.ic_onboarding_night_mode),

    SUGGESTIONS(R.string.ic_onboarding_suggestions, R.drawable.ic_onboarding_suggestions),

    TRACKING(R.string.ic_onboarding_tracking, R.drawable.ic_onboarding_tracking),

    LOGIN(R.string.ic_onboarding_login, R.drawable.ic_onboarding_login),

    CALL_TO_ACTION(R.string.ic_onboarding_cta, R.drawable.ic_onboarding_cta),
}