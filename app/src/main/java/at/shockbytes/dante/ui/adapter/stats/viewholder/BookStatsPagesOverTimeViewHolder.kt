package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.custom.pages.PageRecordDataPoint
import at.shockbytes.dante.ui.custom.pages.PagesDiagramAction
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_pages_over_time.*

class BookStatsPagesOverTimeViewHolder(
        override val containerView: View,
        private val onChangeGoalActionListener: () -> Unit
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.PagesOverTime) {
            when (this) {
                BookStatsViewItem.PagesOverTime.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.PagesOverTime.Present -> {
                    showPagesPerMonth(pagesPerMonths, readingGoal.pagesPerMonth)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_pages_over_time_empty.setVisible(true)
        item_stats_pages_over_time_content.setVisible(false)
    }

    private fun showPagesPerMonth(
            dataPoints: List<PageRecordDataPoint>,
            pagesPerMonthGoal: Int?
    ) {
        item_pages_over_time_empty.setVisible(false)
        item_stats_pages_over_time_content.setVisible(true)

        item_pages_stats_diagram_view.apply {

            headerTitle = if (pagesPerMonthGoal != null) {
                context.getString(R.string.set_goal_header_with_goal, pagesPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = PagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick(onChangeGoalActionListener)
            readingGoal = pagesPerMonthGoal
            updateData(dataPoints)
        }
    }
}
