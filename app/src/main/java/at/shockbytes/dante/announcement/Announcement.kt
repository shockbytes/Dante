package at.shockbytes.dante.announcement

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.navigation.Destination

data class Announcement(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val illustration: Illustration?,
    val action: Action?
) {

    val hasAction: Boolean
        get() = action != null

    sealed class Illustration {

        data class LottieIllustration(
            @RawRes val lottieRes: Int
        ) : Illustration()

        data class ImageIllustration(
            @DrawableRes val drawableRes: Int
        ) : Illustration()
    }

    sealed class Action {

        @get:StringRes
        abstract val actionLabel: Int

        data class OpenUrl(
            @StringRes override val actionLabel: Int = R.string.open,
            val url: String
        ) : Action()

        data class Mail(
            @StringRes override val actionLabel: Int = R.string.action_send_mail,
            val subject: Int
        ) : Action()

        data class OpenScreen(
            @StringRes override val actionLabel: Int = R.string.go_to_screen,
            val destination: Destination
        ) : Action()
    }
}