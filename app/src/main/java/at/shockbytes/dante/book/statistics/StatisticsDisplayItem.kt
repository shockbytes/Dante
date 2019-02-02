package at.shockbytes.dante.book.statistics

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

sealed class StatisticsDisplayItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int
) {

    enum class Align {
        START, END
    }

    class StatisticsDataItem(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        val align: Align,
        vararg val messageArgs: String
    ) : StatisticsDisplayItem(title, icon)

    class StatisticsHeaderItem(
        @StringRes title: Int,
        @DrawableRes icon: Int
    ) : StatisticsDisplayItem(title, icon)
}