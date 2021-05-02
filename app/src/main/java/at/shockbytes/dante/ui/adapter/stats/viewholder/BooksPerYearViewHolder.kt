package at.shockbytes.dante.ui.adapter.stats.viewholder

import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemStatsBooksPerYearBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramOptions
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BooksPerYearViewHolder(
    private val vb: ItemStatsBooksPerYearBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

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
        vb.itemStatsBooksPerYearHeader.setHeaderTitleResource(headerRes)
        vb.itemStatsBooksPerYearEmpty.root.setVisible(true)
        vb.itemStatsBooksPerYearContent.setVisible(false)
    }

    private fun showBooksPerYear(dataPoints: List<BooksAndPageRecordDataPoint>) {
        vb.itemStatsBooksPerYearHeader.setHeaderTitleResource(R.string.statistics_header_books_per_year)
        vb.itemStatsBooksPerYearEmpty.root.setVisible(false)
        vb.itemStatsBooksPerYearContent.setVisible(true)

        vb.itemStatsBooksPerYearDiagramView.apply {
            hideHeader()
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.books_formatted)
            )
        }
    }
}