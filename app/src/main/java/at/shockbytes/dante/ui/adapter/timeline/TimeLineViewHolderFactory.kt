package at.shockbytes.dante.ui.adapter.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemTimeLineBookBinding
import at.shockbytes.dante.databinding.ItemTimeLineHeaderBinding
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.dante.ui.adapter.timeline.viewholder.BookTimeLineViewHolder
import at.shockbytes.dante.ui.adapter.timeline.viewholder.DanteInstallViewHolder
import at.shockbytes.dante.ui.adapter.timeline.viewholder.MonthHeaderViewHolder
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class TimeLineViewHolderFactory(
    private val inflater: LayoutInflater,
    private val imageLoader: ImageLoader
) : ViewHolderTypeFactory<TimeLineItem> {

    override fun type(item: TimeLineItem): Int {
        return when (item) {
            is TimeLineItem.BookTimeLineItem -> R.layout.item_time_line_book
            is TimeLineItem.MonthHeader -> R.layout.item_time_line_header
            TimeLineItem.DanteInstall -> R.layout.item_time_line_install
        }
    }

    override fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<TimeLineItem> {
        return when (viewType) {
            R.layout.item_time_line_book -> BookTimeLineViewHolder(ItemTimeLineBookBinding.inflate(inflater, parent, false), imageLoader)
            R.layout.item_time_line_header -> MonthHeaderViewHolder(ItemTimeLineHeaderBinding.inflate(inflater, parent, false))
            R.layout.item_time_line_install -> DanteInstallViewHolder(inflater.inflate(viewType, parent, false))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }
}