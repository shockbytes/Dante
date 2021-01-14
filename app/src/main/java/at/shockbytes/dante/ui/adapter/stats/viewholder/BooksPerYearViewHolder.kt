package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramOptions
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_books_per_year.*

class BooksPerYearViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {


    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksPerYear) {
            when (this) {
                is BookStatsViewItem.BooksPerYear.Empty -> {
                    showEmptyState(headerRes)
                }
                is BookStatsViewItem.BooksPerYear.Present -> {
                    showBooksPerYear(booksPerYear)
                }
            }
        }
    }

    private fun showEmptyState(@StringRes headerRes: Int) {
        item_stats_books_per_year_header.setHeaderTitleResource(headerRes)
        item_stats_books_per_year_empty.setVisible(true)
        item_stats_books_per_year_content.setVisible(false)
    }

    private fun showBooksPerYear(dataPoints: List<BooksAndPageRecordDataPoint>) {
        item_stats_books_per_year_header.setHeaderTitleResource(R.string.statistics_header_books_per_year)
        item_stats_books_per_year_empty.setVisible(false)
        item_stats_books_per_year_content.setVisible(true)

        item_stats_books_per_year_diagram_view.apply {

            hideHeader()
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.books_formatted)
            )
        }
    }
}