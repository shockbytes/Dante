package at.shockbytes.dante.ui.adapter.stats

import android.content.Context
import android.view.LayoutInflater
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsAdapter(
    context: Context,
    private val imageLoader: ImageLoader
) : MultiViewHolderBaseAdapter<BookStatsItem>(context) {

    fun updateData(items: List<BookStatsItem>) {
        data.clear()
        data.addAll(items)

        notifyDataSetChanged()
    }

    override val vhFactory: ViewHolderTypeFactory<BookStatsItem>
        get() = StatsViewHolderFactory(LayoutInflater.from(context), imageLoader)
}