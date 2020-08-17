package at.shockbytes.dante.ui.adapter.stats

import android.content.Context
import android.view.LayoutInflater
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val onChangeGoalActionListener: () -> Unit
) : MultiViewHolderBaseAdapter<BookStatsViewItem>(context) {

    fun updateData(items: List<BookStatsViewItem>) {
        data.clear()
        data.addAll(items)

        notifyDataSetChanged()
    }

    override val vhFactory: ViewHolderTypeFactory<BookStatsViewItem>
        get() = StatsViewHolderFactory(LayoutInflater.from(context), imageLoader, onChangeGoalActionListener)
}