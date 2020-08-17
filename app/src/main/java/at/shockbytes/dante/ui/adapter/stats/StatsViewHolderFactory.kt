package at.shockbytes.dante.ui.adapter.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.adapter.stats.viewholder.*
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsViewHolderFactory(
    private val inflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onChangeGoalActionListener: () -> Unit
) : ViewHolderTypeFactory<BookStatsViewItem> {

    override fun type(item: BookStatsViewItem): Int {
        return item.layoutId
    }

    override fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<BookStatsViewItem> {
        return when (viewType) {
            R.layout.item_stats_books_and_pages -> BookStatsBookAndPagesViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_reading_duration -> BookStatsReadingDurationViewHolder(inflater.inflate(viewType, parent, false), imageLoader)
            R.layout.item_stats_favorites -> BookStatsFavoritesViewHolder(inflater.inflate(viewType, parent, false), imageLoader)
            R.layout.item_stats_languages -> BookStatsLanguageViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_others -> BookStatsOthersViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_labels -> BookStatsLabelsViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_pages_over_time -> BookStatsPagesOverTimeViewHolder(inflater.inflate(viewType, parent, false), onChangeGoalActionListener)
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }
}