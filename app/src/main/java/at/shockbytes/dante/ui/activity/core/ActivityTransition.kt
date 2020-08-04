package at.shockbytes.dante.ui.activity.core

import android.transition.Explode
import android.transition.Fade
import android.transition.Transition

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
    }
}