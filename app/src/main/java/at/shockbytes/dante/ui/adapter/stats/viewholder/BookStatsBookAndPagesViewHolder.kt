package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemStatsBooksAndPagesBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.BooksPagesInfo
import at.shockbytes.dante.ui.custom.rbc.RelativeBarChartData
import at.shockbytes.dante.ui.custom.rbc.RelativeBarChartEntry
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class BookStatsBookAndPagesViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    private val vb = ItemStatsBooksAndPagesBinding.bind(containerView)

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksAndPages) {
            when (this) {
                BookStatsViewItem.BooksAndPages.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.BooksAndPages.Present -> {
                    showBooksAndPages(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsBooksAndPagesEmpty.root.setVisible(true)
        vb.itemStatsBooksAndPagesContent.setVisible(false)
    }

    private fun showBooksAndPages(content: BookStatsViewItem.BooksAndPages.Present) {
        vb.itemStatsBooksAndPagesEmpty.root.setVisible(false)
        vb.itemStatsBooksAndPagesContent.setVisible(true)

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

        vb.rbcItemStatsBooks.post {
            vb.rbcItemStatsBooks.setChartData(RelativeBarChartData(entries))
        }

        vb.tvItemStatsBooksWaiting.text = containerView.context.getString(R.string.books_waiting, books.waiting)
        vb.tvItemStatsBooksReading.text = containerView.context.getString(R.string.books_reading, books.reading)
        vb.tvItemStatsBooksRead.text = containerView.context.getString(R.string.books_read, books.read)
    }

    private fun setPages(pages: BooksPagesInfo.Pages) {

        val entries = listOf(
            RelativeBarChartEntry(
                pages.waiting.toFloat(),
                R.color.tabcolor_upcoming
            ),
            RelativeBarChartEntry(
                pages.read.toFloat(),
                R.color.tabcolor_done
            )
        )

        vb.rbcItemStatsPages.post {
            vb.rbcItemStatsPages.setChartData(RelativeBarChartData(entries))
        }

        vb.tvItemStatsPagesWaiting.text = containerView.context.getString(R.string.pages_waiting, pages.waiting)
        vb.tvItemStatsPagesRead.text = containerView.context.getString(R.string.pages_read, pages.read)
    }
}