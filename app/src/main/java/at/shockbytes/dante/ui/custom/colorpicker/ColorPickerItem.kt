package at.shockbytes.dante.ui.custom.colorpicker

import androidx.annotation.ColorRes

data class ColorPickerItem(
    @ColorRes val colorRes: Int,
    val isSelected: Boolean = false
)
