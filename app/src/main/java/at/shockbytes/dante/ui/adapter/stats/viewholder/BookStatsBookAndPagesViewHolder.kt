package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.stats.BooksPagesInfo
import at.shockbytes.dante.ui.custom.rbc.RelativeBarChartData
import at.shockbytes.dante.ui.custom.rbc.RelativeBarChartEntry
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_books_and_pages.*

class BookStatsBookAndPagesViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.BooksAndPages) {
            when (this) {
                BookStatsItem.BooksAndPages.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.BooksAndPages.Present -> {
                    showBooksAndPages(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_stats_books_and_pages_empty.setVisible(true)
        item_stats_books_and_pages_content.setVisible(false)
    }

    private fun showBooksAndPages(content: BookStatsItem.BooksAndPages.Present) {
        item_stats_books_and_pages_empty.setVisible(false)
        item_stats_books_and_pages_content.setVisible(true)

        with(content) {
            setBooks(booksAndPages.books)
            setPages(booksAndPages.pages)
        }
    }

    private fun setBooks(books: BooksPagesInfo.Books) {

        val entries = listOf(
            RelativeBarChartEntry(
                books.waiting.toFloat(),
                R.color.tabcolor_upcoming
            ),
            RelativeBarChartEntry(
                books.reading.toFloat(),
                R.color.tabcolor_current
            ),
            RelativeBarChartEntry(
                books.read.toFloat(),
                R.color.tabcolor_done
            )
        )

        rbc_item_stats_books.post {
            rbc_item_stats_books.setChartData(RelativeBarChartData(entries))
        }

        tv_item_stats_books_waiting.text = containerView.context.getString(R.string.books_waiting, books.waiting)
        tv_item_stats_books_reading.text = containerView.context.getString(R.string.books_reading, books.reading)
        tv_item_stats_books_read.text = containerView.context.getString(R.string.books_read, books.read)
    }

    private fun setPages(pages: BooksPagesInfo.Pages) {

        val entries = listOf(
            RelativeBarChartEntry(
                pages.waiting.toFloat(),
                R.color.tabcolor_upcoming_dark
            ),
            RelativeBarChartEntry(
                pages.read.toFloat(),
                R.color.tabcolor_done_dark
            )
        )

        rbc_item_stats_pages.post {
            rbc_item_stats_pages.setChartData(RelativeBarChartData(entries))
        }

        tv_item_stats_pages_waiting.text = containerView.context.getString(R.string.pages_waiting, pages.waiting)
        tv_item_stats_pages_read.text = containerView.context.getString(R.string.pages_read, pages.read)
    }
}