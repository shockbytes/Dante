package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.seasonal_theme_view.view.*

class SeasonalThemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.seasonal_theme_view, this)
        isClickable = false
        isFocusable = false
    }

    fun setSeasonalTheme(theme: SeasonalTheme) {
        when (theme) {
            is SeasonalTheme.LottieAssetsTheme -> {
                setVisible(true)
                // Setting the same animation somehow causes issues, check if it is already animating
                if (!lottie_seasonal_theme.isAnimating) {
                    lottie_seasonal_theme.apply {
                        speed = theme.lottieSpeed
                        setAnimation(theme.lottieAsset)
                    }
                }
            }
            SeasonalTheme.NoTheme -> setVisible(false)
        }
    }
}