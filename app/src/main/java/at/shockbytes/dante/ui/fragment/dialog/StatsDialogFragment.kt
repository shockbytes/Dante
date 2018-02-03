package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 29.08.2016.
 */
class StatsDialogFragment : BaseDialogFragment() {

    private val txtPagesRead: TextView by bindView(R.id.dialogfragment_stats_txt_pages_read)
    private val txtPagesWaiting: TextView by bindView(R.id.dialogfragment_stats_txt_pages_waiting)
    private val txtBooksRead: TextView by bindView(R.id.dialogfragment_stats_txt_books_read)
    private val txtBooksWaiting: TextView by bindView(R.id.dialogfragment_stats_txt_books_waiting)
    private val txtFastestBook: TextView by bindView(R.id.dialogfragment_stats_txt_fastest_book)
    private val txtSlowestBook: TextView by bindView(R.id.dialogfragment_stats_txt_slowest_book)
    private val txtAvgBooks: TextView by bindView(R.id.dialogfragment_stats_txt_avg_books)
    private val txtMostReadingMonth: TextView by bindView(R.id.dialogfragment_stats_txt_months_most_books)

    @Inject
    protected lateinit var bookManager: BookManager

    private val statsView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_stats, null, false)

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.label_stats)
                .setIcon(R.drawable.ic_stats)
                .setView(statsView)
                .setCancelable(true)
                .create()
    }

    override fun onResume() {
        super.onResume()
        showStats()
    }

    private fun showStats() {
        bookManager.statistics.subscribe { stats ->

            txtPagesRead.text = getString(R.string.stats_pages_read, stats.pagesRead)
            txtPagesWaiting.text = getString(R.string.stats_pages_waiting, stats.pagesWaiting)
            txtBooksRead.text = getString(R.string.stats_books_read, stats.booksRead)
            txtBooksWaiting.text = getString(R.string.stats_books_waiting, stats.booksWaiting)

            val textFastestBook = if (stats.fastestBook != null) {
                getString(R.string.stats_duration_book,
                        stats.fastestBook.days, stats.fastestBook.bookName)
            } else {
                getString(R.string.stats_book_duration_default)
            }
            txtFastestBook.text = textFastestBook

            val textSlowestBook = if (stats.slowestBook != null) {
                getString(R.string.stats_duration_book,
                        stats.slowestBook.days, stats.slowestBook.bookName)
            } else {
                getString(R.string.stats_book_duration_default)
            }
            txtSlowestBook.text = textSlowestBook

            txtAvgBooks.text = getString(R.string.stats_avg_books_per_month,
                    stats.avgBooksPerMonth.toString())
            val textMostReadingMonth = if (stats.mostReadingMonth != null) {
                getString(R.string.stats_most_reading_month,
                        stats.mostReadingMonth.finishedBooks, stats.mostReadingMonth.monthAsString)
            } else {
                getString(R.string.stats_book_duration_default)
            }
            txtMostReadingMonth.text = textMostReadingMonth
        }
    }

    companion object {

        fun newInstance(): StatsDialogFragment {
            val fragment = StatsDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
