package at.shockbytes.dante.ui.activity.core

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

/**
 * @author Martin Macheiner
 * Date: 02.01.2018.
 */

abstract class TintableBackNavigableActivity : BackNavigableActivity() {

    private val abDefColor = R.color.colorPrimary
    private val abTextDefColor = android.R.color.white
    private val sbDefColor = R.color.colorPrimaryDark

    private var upIndicator: Int = R.drawable.ic_back_arrow

    @JvmOverloads
    fun setTintableHomeAsUpIndicator(@DrawableRes indicator: Int = upIndicator,
                                     tint: Boolean = false,
                                     @ColorInt tintColor: Int = Color.WHITE) {

        upIndicator = indicator // Store for next time if just tinting is necessary
        if (tint) {
            val drawable = ContextCompat.getDrawable(applicationContext, indicator)
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
            supportActionBar?.setHomeAsUpIndicator(drawable)
        } else {
            supportActionBar?.setHomeAsUpIndicator(indicator)
        }
    }

    fun tintSystemBarsWithText(@ColorInt actionBarColor: Int?, @ColorInt actionBarTextColor: Int?,
                               @ColorInt statusBarColor: Int?, title: String?) {

        // Default initialize if not set
        val abColor = actionBarColor ?: ContextCompat.getColor(applicationContext, abDefColor)
        val abtColor = actionBarTextColor ?: ContextCompat.getColor(applicationContext, abTextDefColor)
        val sbColor = statusBarColor ?: ContextCompat.getColor(applicationContext, sbDefColor)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(abColor))
        val text = SpannableString(title)
        text.setSpan(ForegroundColorSpan(abtColor), 0, text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        supportActionBar?.title = text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = sbColor
        }

        setTintableHomeAsUpIndicator(tint = true, tintColor = abtColor)
    }

}