package at.shockbytes.dante.ui.custom.rbc

import androidx.annotation.ColorRes

data class RelativeBarChartEntry(
    val value: Float,
    @ColorRes val color: Int,
    val description: CharSequence = ""
)