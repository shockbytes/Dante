package at.shockbytes.dante.core.book.statistics

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

@Deprecated("Use BookStatsItem instead")
sealed class StatisticsDisplayItem {

    @get:StringRes
    abstract val title: Int

    @get:DrawableRes
    abstract val icon: Int

    data class StatisticsDataItem(
        override val title: Int,
        override val icon: Int,
        @ColorRes val tintColorRes: Int,
        val align: Align,
        val messageArgs: List<String>
    ) : StatisticsDisplayItem()

    data class StatisticsHeaderItem(
        override val title: Int,
        override val icon: Int
    ) : StatisticsDisplayItem()

    enum class Align {
        START, END
    }
}