package at.shockbytes.dante.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.HapticFeedbackConstants
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.ColorUtils
import at.shockbytes.dante.util.DanteUtils.dpToPixelF
import at.shockbytes.dante.util.getBoldThemeFont
import at.shockbytes.dante.util.isNightModeEnabled
import com.google.android.material.chip.Chip

object ChipFactory {

    fun buildChipViewFromLabel(
        context: Context,
        label: BookLabel,
        onLabelClickedListener: ((BookLabel) -> Unit)?,
        showCloseIcon: Boolean = false,
        closeIconClickCallback: ((BookLabel) -> Unit)? = null
    ): Chip {

        val chipColor = if (context.isNightModeEnabled()) {
            ColorUtils.desaturateAndDevalue(Color.parseColor(label.hexColor), by = 0.25f)
        } else {
            Color.parseColor(label.hexColor)
        }

        return Chip(context).apply {
            chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
            text = label.title
            typeface = context.getBoldThemeFont()
            setTextColor(chipColor)
            chipStrokeColor = ColorStateList.valueOf(chipColor)
            chipStrokeWidth = context.dpToPixelF(1)
            setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onLabelClickedListener?.invoke(label)
            }

            if (showCloseIcon) {
                closeIconTint = ColorStateList.valueOf(chipColor)
                isCloseIconVisible = true
                setOnCloseIconClickListener { v ->
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    closeIconClickCallback?.invoke(label)
                }
            }
        }
    }
}