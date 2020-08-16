package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.PagesPerMonth
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_pages_over_time.*

class BookStatsPagesOverTimeViewHolder(override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.PagesOverTime) {
            when (this) {
                BookStatsViewItem.PagesOverTime.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.PagesOverTime.Present -> {
                    showPagesPerMonth(pagesPerMonths)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_pages_over_time_empty.setVisible(true)
        item_stats_pages_over_time_content.setVisible(false)
    }

    private fun showPagesPerMonth(pagesPerMonths: List<PagesPerMonth>) {
        item_pages_over_time_empty.setVisible(false)
        item_stats_pages_over_time_content.setVisible(true)
        // TODO
    }
}