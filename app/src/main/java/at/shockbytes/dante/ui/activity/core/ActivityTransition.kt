package at.shockbytes.dante.ui.activity.core

import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.view.Gravity

data class ActivityTransition(
    val enterTransition: Transition,
    val exitTransition: Transition
) {

    companion object {

        fun default(): ActivityTransition {
            return ActivityTransition(
                enterTransition = Explode(),
                exitTransition = Fade()
            )
        }

        fun slideFromBottom(): ActivityTransition {
            return ActivityTransition(
                enterTransition = Slide(Gravity.BOTTOM),
                exitTransition = Slide(Gravity.BOTTOM)
            )
        }

        fun none(): ActivityTransition? = null
    }
}