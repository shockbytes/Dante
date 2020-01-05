package at.shockbytes.dante.ui.adapter.timeline.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_time_line_header.*

class MonthHeaderViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<TimeLineItem>(containerView), LayoutContainer {

    override fun bindToView(content: TimeLineItem, position: Int) {
        with (content as TimeLineItem.MonthHeader) {
            val monthStr = containerView.context.resources.getStringArray(R.array.months)[month - 1]
            tv_time_line_header.text = containerView.context.getString(R.string.date_month_and_year, monthStr, year)
        }
    }
}