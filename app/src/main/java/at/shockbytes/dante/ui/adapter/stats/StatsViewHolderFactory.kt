package at.shockbytes.dante.ui.adapter.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.ui.adapter.stats.viewholder.BookStatsBookAndPagesViewHolder
import at.shockbytes.dante.ui.adapter.stats.viewholder.BookStatsFavoritesViewHolder
import at.shockbytes.dante.ui.adapter.stats.viewholder.BookStatsLanguageViewHolder
import at.shockbytes.dante.ui.adapter.stats.viewholder.BookStatsOthersViewHolder
import at.shockbytes.dante.ui.adapter.stats.viewholder.BookStatsReadingDurationViewHolder
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsViewHolderFactory(
    private val inflater: LayoutInflater,
    private val imageLoader: ImageLoader
) : ViewHolderTypeFactory<BookStatsItem> {

    override fun type(item: BookStatsItem): Int {
        return when (item) {
            is BookStatsItem.BooksAndPages -> R.layout.item_stats_books_and_pages
            is BookStatsItem.ReadingDuration -> R.layout.item_stats_reading_duration
            is BookStatsItem.Favorites -> R.layout.item_stats_favorites
            is BookStatsItem.Languages -> R.layout.item_stats_languages
            is BookStatsItem.Others -> R.layout.item_stats_others
        }
    }

    override fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<BookStatsItem> {
        return when (viewType) {
            R.layout.item_stats_books_and_pages -> BookStatsBookAndPagesViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_reading_duration -> BookStatsReadingDurationViewHolder(inflater.inflate(viewType, parent, false), imageLoader)
            R.layout.item_stats_favorites -> BookStatsFavoritesViewHolder(inflater.inflate(viewType, parent, false), imageLoader)
            R.layout.item_stats_languages -> BookStatsLanguageViewHolder(inflater.inflate(viewType, parent, false))
            R.layout.item_stats_others -> BookStatsOthersViewHolder(inflater.inflate(viewType, parent, false))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }
}