package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemStatsBooksPerYearBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramOptions
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class BooksPerYearViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    private val vb = ItemStatsBooksPerYearBinding.bind(containerView)

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