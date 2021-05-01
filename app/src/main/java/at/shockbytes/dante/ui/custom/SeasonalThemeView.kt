package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.SeasonalThemeViewBinding
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.dante.util.setVisible

class SeasonalThemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vb: SeasonalThemeViewBinding
        get() = SeasonalThemeViewBinding.inflate(context.layoutInflater(), this, true)

    init {
        isClickable = false
        isFocusable = false
    }

    fun setSeasonalTheme(theme: SeasonalTheme) {
        when (theme) {
            is SeasonalTheme.LottieAssetsTheme -> {
                setVisible(true)
                // Setting the same animation somehow causes issues, check if it is already animating
                if (!vb.lottieSeasonalTheme.isAnimating) {
                    vb.lottieSeasonalTheme.apply {
                        speed = theme.lottieSpeed
                        setAnimation(theme.lottieAsset)
                    }
                }
            }
            SeasonalTheme.NoTheme -> setVisible(false)
        }
    }
}