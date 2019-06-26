package at.shockbytes.dante.announcement

import androidx.annotation.RawRes
import androidx.annotation.StringRes

data class Announcement(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val illustration: Illustration?,
    val action: Action?
) {

    sealed class Illustration {

        data class LottieIllustration(
            @RawRes val lottieRes: Int
        ) : Illustration()
    }

    sealed class Action {

        data class OpenUrl(val url: String) : Action()
    }
}