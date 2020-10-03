package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramAction
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramView
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewOptions
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_pages_over_time.*

class BookStatsPagesOverTimeViewHolder(
        override val containerView: View,
        private val onChangeGoalActionListener: (ReadingGoalType) -> Unit
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksAndPagesOverTime) {
            when (this) {
                is BookStatsViewItem.BooksAndPagesOverTime.Empty -> {
                    showEmptyState(headerRes)
                }
                is BookStatsViewItem.BooksAndPagesOverTime.Present.Pages -> {
                    showPagesPerMonth(pagesPerMonths, readingGoal.pagesPerMonth)
                }
                is BookStatsViewItem.BooksAndPagesOverTime.Present.Books -> {
                    showBooksPerMonth(booksPerMonths, readingGoal.booksPerMonth)
                }
            }
        }
    }

    private fun showEmptyState(@StringRes headerRes: Int) {
        item_books_pages_over_time_header.setHeaderTitleResource(headerRes)
        item_pages_over_time_empty.setVisible(true)
        item_stats_pages_over_time_content.setVisible(false)
    }

    private fun showPagesPerMonth(
            dataPoints: List<BooksAndPageRecordDataPoint>,
            pagesPerMonthGoal: Int?
    ) {
        item_books_pages_over_time_header.setHeaderTitleResource(R.string.statistics_header_pages_over_time)
        item_pages_over_time_empty.setVisible(false)
        item_stats_pages_over_time_content.setVisible(true)

        item_pages_stats_diagram_view.apply {

            headerTitle = if (pagesPerMonthGoal != null) {
                context.getString(R.string.set_pages_goal_header_with_goal, pagesPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.PAGES)
            }
            readingGoal(pagesPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.PAGES)
            setData(
                    dataPoints,
                    options = MarkerViewOptions.ofDataPoints(dataPoints, R.string.pages_formatted)
            )
        }
    }

    private fun showBooksPerMonth(
            dataPoints: List<BooksAndPageRecordDataPoint>,
            booksPerMonthGoal: Int?
    ) {
        item_books_pages_over_time_header.setHeaderTitleResource(R.string.statistics_header_books_over_time)
        item_pages_over_time_empty.setVisible(false)
        item_stats_pages_over_time_content.setVisible(true)

        item_pages_stats_diagram_view.apply {

            headerTitle = if (booksPerMonthGoal != null) {
                context.getString(R.string.set_books_goal_header_with_goal, booksPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.BOOKS)
            }
            readingGoal(booksPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.BOOKS)
            setData(
                    dataPoints,
                    options = MarkerViewOptions.ofDataPoints(dataPoints, R.string.books_formatted)
            )
        }
    }
}
