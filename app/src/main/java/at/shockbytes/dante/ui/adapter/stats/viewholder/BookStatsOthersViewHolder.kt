package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.stats.MostActiveMonth
import at.shockbytes.dante.util.roundDouble
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_others.*

class BookStatsOthersViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.Others) {
            when (this) {
                BookStatsItem.Others.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.Others.Present -> {
                    showInformation(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_stats_others_empty.setVisible(true)
        item_stats_others_content.setVisible(false)
    }

    private fun showInformation(content: BookStatsItem.Others.Present) {
        item_stats_others_empty.setVisible(false)
        item_stats_others_content.setVisible(true)

        with(content) {
            setAverageBooksPerMonth(averageBooksPerMonth)
            setMostActiveMonth(mostActiveMonth)
            setAverageRating(averageRating)
        }
    }

    private fun setAverageBooksPerMonth(averageBooksPerMonth: Double) {
        tv_item_stats_others_average_books_content.text = averageBooksPerMonth.roundDouble(2).toString()
    }

    private fun setMostActiveMonth(mostActiveMonth: MostActiveMonth?) {

        if (mostActiveMonth != null) {
            tv_item_stats_others_most_active_month_content.text = mostActiveMonth.finishedBooks.toString()
            tv_item_stats_others_most_active_month_description.text = containerView.context.getString(R.string.most_active_month, mostActiveMonth.monthAsString)
        } else {
            tv_item_stats_others_most_active_month_content.text = "-"
            tv_item_stats_others_most_active_month_description.text = "---"
        }
    }

    private fun setAverageRating(averageRating: Double) {

        rating_item_stats_others.apply {
            rating = averageRating.toFloat()
            stepSize = 0.1f
        }

        val roundedRating = averageRating.roundDouble(1).toString()
        tv_item_stats_others_rating_content.text = containerView.context.getString(R.string.stars_with_placeholder, roundedRating)
    }
}