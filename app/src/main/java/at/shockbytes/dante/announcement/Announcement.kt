package at.shockbytes.dante.announcement

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

data class Announcement(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val illustration: Illustration?
) {

    sealed class Illustration {

        data class DrawableIllustration(
            @DrawableRes val drawableRes: Int
        ) : Illustration()

        data class LottieIllustration(
            @RawRes val lottieRes: Int
        ) : Illustration()
    }
}