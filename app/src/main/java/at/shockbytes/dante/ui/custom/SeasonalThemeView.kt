package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.seasonal_theme_view.view.*

class SeasonalThemeView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.seasonal_theme_view, this)
        isClickable = false
        isFocusable = false
    }

    fun setSeasonalTheme(theme: SeasonalTheme): Unit = when (theme) {
        is SeasonalTheme.LottieTheme -> {
            setVisible(true)
            lottie_seasonal_theme.setAnimation(theme.lottieAsset)
        }
        SeasonalTheme.NoTheme -> setVisible(false)
    }
}