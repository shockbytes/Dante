package at.shockbytes.dante.ui.adapter.timeline

import android.content.Context
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter

class TimeLineAdapter(context: Context) : MultiViewHolderBaseAdapter<TimeLineItem>(context) {

    override val vhFactory = TimeLineViewHolderFactory()
}