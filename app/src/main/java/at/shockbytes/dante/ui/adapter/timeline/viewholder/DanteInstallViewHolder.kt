package at.shockbytes.dante.ui.adapter.timeline.viewholder

import android.view.View
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class DanteInstallViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<TimeLineItem>(containerView), LayoutContainer {

    override fun bindToView(content: TimeLineItem, position: Int) = Unit
}