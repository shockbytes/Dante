package at.shockbytes.dante.ui.adapter.stats.viewholder

import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemStatsPagesOverTimeBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramAction
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramOptions
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramView
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsPagesOverTimeViewHolder(
    private val vb: ItemStatsPagesOverTimeBinding,
    private val onChangeGoalActionListener: (ReadingGoalType) -> Unit
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

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
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(headerRes)
        vb.itemPagesOverTimeEmpty.root.setVisible(true)
        vb.itemStatsPagesOverTimeContent.setVisible(false)
    }

    private fun showPagesPerMonth(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        pagesPerMonthGoal: Int?
    ) {
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(R.string.statistics_header_pages_over_time)
        vb.itemPagesOverTimeEmpty.root.setVisible(false)
        vb.itemStatsPagesOverTimeContent.setVisible(true)

        vb.itemPagesStatsDiagramView.apply {

            headerTitle = if (pagesPerMonthGoal != null) {
                context.getString(R.string.set_pages_goal_header_with_goal, pagesPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.PAGES)
            }
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.pages_formatted)
            )
            readingGoal(pagesPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.PAGES)
        }
    }

    private fun showBooksPerMonth(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        booksPerMonthGoal: Int?
    ) {
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(R.string.statistics_header_books_over_time)
        vb.itemPagesOverTimeEmpty.root.setVisible(false)
        vb.itemStatsPagesOverTimeContent.setVisible(true)

        vb.itemPagesStatsDiagramView.apply {

            headerTitle = if (booksPerMonthGoal != null) {
                context.getString(R.string.set_books_goal_header_with_goal, booksPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.BOOKS)
            }
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.books_formatted)
            )
            readingGoal(booksPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.BOOKS)
        }
    }
}
