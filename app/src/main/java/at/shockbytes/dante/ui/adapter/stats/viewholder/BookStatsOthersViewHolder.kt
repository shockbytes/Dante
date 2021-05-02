package at.shockbytes.dante.ui.adapter.stats.viewholder

import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemStatsOthersBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.MostActiveMonth
import at.shockbytes.dante.util.roundDouble
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsOthersViewHolder(
    private val vb: ItemStatsOthersBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.Others) {
            when (this) {
                BookStatsViewItem.Others.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.Others.Present -> {
                    showInformation(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsOthersEmpty.root.setVisible(true)
        vb.itemStatsOthersContent.setVisible(false)
    }

    private fun showInformation(content: BookStatsViewItem.Others.Present) {
        vb.itemStatsOthersEmpty.root.setVisible(false)
        vb.itemStatsOthersContent.setVisible(true)

        with(content) {
            setAverageBooksPerMonth(averageBooksPerMonth)
            setMostActiveMonth(mostActiveMonth)
            setAverageRating(averageRating)
        }
    }

    private fun setAverageBooksPerMonth(averageBooksPerMonth: Double) {
        vb.tvItemStatsOthersAverageBooksContent.text = averageBooksPerMonth.roundDouble(2).toString()
    }

    private fun setMostActiveMonth(mostActiveMonth: MostActiveMonth?) {

        if (mostActiveMonth != null) {
            vb.tvItemStatsOthersMostActiveMonthContent.text = mostActiveMonth.finishedBooks.toString()
            vb.tvItemStatsOthersMostActiveMonthDescription.text = vb.root.context.getString(R.string.most_active_month, mostActiveMonth.monthAsString)
        } else {
            vb.tvItemStatsOthersMostActiveMonthContent.text = "-"
            vb.tvItemStatsOthersMostActiveMonthDescription.text = "---"
        }
    }

    private fun setAverageRating(averageRating: Double) {

        vb.ratingItemStatsOthers.apply {
            rating = averageRating.toFloat()
            stepSize = 0.1f
        }

        val roundedRating = averageRating.roundDouble(1).toString()
        vb.tvItemStatsOthersRatingContent.text = vb.root.context.getString(R.string.stars_with_placeholder, roundedRating)
    }
}