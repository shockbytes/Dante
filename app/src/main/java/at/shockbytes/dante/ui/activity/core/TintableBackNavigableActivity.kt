package at.shockbytes.dante.ui.activity.core

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import at.shockbytes.dante.R
import at.shockbytes.dante.util.DanteUtils

/**
 * @author Martin Macheiner
 * Date: 02.01.2018.
 */

abstract class TintableBackNavigableActivity : BackNavigableActivity() {

    private val abDefColor = R.color.actionBarItemColor
    private val abTextDefColor = android.R.color.white
    private val sbDefColor = R.color.colorPrimaryDark

    private var upIndicator: Int = R.drawable.ic_back_arrow

    @ColorInt
    private var textColor: Int = Color.parseColor("#212121")

    @JvmOverloads
    fun tintHomeAsUpIndicator(@DrawableRes indicator: Int = upIndicator,
                              tint: Boolean = false,
                              @ColorInt tintColor: Int = Color.WHITE) {

        upIndicator = indicator // Store for next time if just tinting is necessary
        if (tint) {
            val drawable = DanteUtils.vector2Drawable(applicationContext, indicator)
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            supportActionBar?.setHomeAsUpIndicator(drawable)
        } else {
            supportActionBar?.setHomeAsUpIndicator(indicator)
        }
    }

    fun tintTitle(title: String) {
        val text = SpannableString(title)
        text.setSpan(ForegroundColorSpan(textColor), 0, text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        supportActionBar?.title = text
    }

    fun tintSystemBarsWithText(@ColorInt actionBarColor: Int?, @ColorInt actionBarTextColor: Int?,
                               @ColorInt statusBarColor: Int?, title: String? = null,
                               animated: Boolean = false,
                               useSameColorsForBoth: Boolean = true) {

        // Default initialize if not set
        val abColor = actionBarColor ?: ContextCompat.getColor(applicationContext, abDefColor)
        var sbColor = statusBarColor ?: ContextCompat.getColor(applicationContext, sbDefColor)
        val abtColor = actionBarTextColor ?: ContextCompat.getColor(applicationContext, abTextDefColor)
        textColor = actionBarTextColor ?: textColor

        if (useSameColorsForBoth) {
           sbColor = abColor
        }

        // Set and tint text of action bar
        val newTitle = title ?: supportActionBar?.title
        tintTitle(newTitle?.toString() ?: "")

        if (animated) {
            tintSystemBarsAnimated(abColor, sbColor)
        } else {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(abColor))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = sbColor
            }
        }
        tintHomeAsUpIndicator(tint = true, tintColor = abtColor)
    }

    private fun tintSystemBarsAnimated(@ColorInt newColor: Int, @ColorInt newColorDark: Int) {

        val primary = ContextCompat.getColor(this, R.color.colorPrimary)
        val primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        val animatorToolbar = ValueAnimator.ofObject(ArgbEvaluator(), primary, newColor)
                .setDuration(300)
        animatorToolbar.addUpdateListener { valueAnimator ->
            supportActionBar?.setBackgroundDrawable(ColorDrawable(valueAnimator.animatedValue as Int))
        }
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), primaryDark, newColorDark)
                .setDuration(300)
        // Suppress lint, because we are only setting applyListener, when api is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnimation.addUpdateListener { valueAnimator ->
                window.statusBarColor = valueAnimator.animatedValue as Int
            }
        }

        val set = AnimatorSet()
        set.playTogether(animatorToolbar, colorAnimation)
        set.start()
    }

}