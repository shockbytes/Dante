package at.shockbytes.dante.ui.view

import android.view.View
import android.view.animation.Interpolator

object AnimationUtils {

    fun detailEnterAnimation(
        animationList: List<View>,
        duration: Long = 300,
        initialDelay: Long = 300,
        durationBetweenAnimations: Long = 50,
        interpolator: Interpolator
    ) {

        animationList.forEach { v ->
            v.apply {
                alpha = 0f
                scaleX = 0.9f
                scaleY = 0.9f
                translationY = 50f
            }
        }

        animationList.forEachIndexed { index, view ->
            view.animate()
                .scaleY(1f)
                .scaleX(1f)
                .alpha(1f)
                .translationY(0f)
                .setInterpolator(interpolator)
                .setStartDelay((initialDelay + (index * durationBetweenAnimations)))
                .setDuration(duration)
                .withEndAction {

                    // If anim failed, set it in the end
                    view.apply {
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                        translationY = 0f
                    }
                }
                .start()
        }
    }
}