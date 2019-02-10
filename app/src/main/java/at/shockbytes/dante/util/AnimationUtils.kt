package at.shockbytes.dante.util

import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator

object AnimationUtils {

    fun detailEnterAnimation(
        animationList: List<View>,
        duration: Long = 300,
        initialDelay: Long = 300,
        interpolator: Interpolator = OvershootInterpolator(2f)
    ) {

        animationList.forEach {
            it.alpha = 0f; it.scaleX = 0.3f; it.scaleY = 0.3f
        }

        animationList.forEachIndexed { index, view ->
            view.animate().scaleY(1f).scaleX(1f).alpha(1f)
                    .setInterpolator(interpolator)
                    .setStartDelay((initialDelay + (index * 100L)))
                    .setDuration(duration)
                    .withEndAction { view.alpha = 1f; view.scaleX = 1f; view.scaleY = 1f } // <-- If anim failed, set it in the end
                    .start()
        }
    }
}