package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import at.shockbytes.dante.R

class BackgroundSelectionView(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {

    init {
        setBackgroundResource(R.drawable.bg_round_border)
        alpha = 0f
    }

    fun onChanged(isActivated: Boolean) {
        val alphaValue = if (isActivated) 1f else 0f

        animate().alpha(alphaValue).start()
    }
}