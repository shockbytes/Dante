package at.shockbytes.dante.book.statistics

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class StatisticsDisplayItem {

    @get:StringRes
    abstract val title: Int

    @get:DrawableRes
    abstract val icon: Int

    data class StatisticsDataItem(
        override val title: Int,
        override val icon: Int,
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