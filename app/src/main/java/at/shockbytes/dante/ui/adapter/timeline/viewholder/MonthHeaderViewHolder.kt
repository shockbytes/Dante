package at.shockbytes.dante.ui.adapter.timeline.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemTimeLineHeaderBinding
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class MonthHeaderViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<TimeLineItem>(containerView), LayoutContainer {

    private val vb = ItemTimeLineHeaderBinding.bind(containerView)

    override fun bindToView(content: TimeLineItem, position: Int) {
        with(content as TimeLineItem.MonthHeader) {
            val monthStr = containerView.context.resources.getStringArray(R.array.months)[month - 1]
            vb.tvTimeLineHeader.text = containerView.context.getString(R.string.date_month_and_year, monthStr, year.toString())
        }
    }
}