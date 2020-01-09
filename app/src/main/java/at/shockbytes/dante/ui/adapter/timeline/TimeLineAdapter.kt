package at.shockbytes.dante.ui.adapter.timeline

import android.content.Context
import android.view.LayoutInflater
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter

class TimeLineAdapter(
    context: Context,
    imageLoader: ImageLoader
) : MultiViewHolderBaseAdapter<TimeLineItem>(context) {

    override val vhFactory = TimeLineViewHolderFactory(LayoutInflater.from(context), imageLoader)
}