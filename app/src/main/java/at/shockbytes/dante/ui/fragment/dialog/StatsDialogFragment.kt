package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatDrawableManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindViews
import javax.inject.Inject


/**
 * @author Martin Macheiner
 * Date: 29.08.2016.
 */
class StatsDialogFragment : BaseDialogFragment() {

    private val statsViews: List<TextView>by bindViews(
            R.id.dialogfragment_stats_txt_pages_read,
            R.id.dialogfragment_stats_txt_pages_waiting,
            R.id.dialogfragment_stats_txt_books_read,
            R.id.dialogfragment_stats_txt_books_waiting,
            R.id.dialogfragment_stats_txt_fastest_book,
            R.id.dialogfragment_stats_txt_slowest_book,
            R.id.dialogfragment_stats_txt_avg_books,
            R.id.dialogfragment_stats_txt_months_most_books)

    private val drawableResList: List<Int> = listOf(
            R.drawable.ic_pages_colored,
            R.drawable.ic_pages,
            R.drawable.ic_popup_done,
            R.drawable.ic_popup_upcoming,
            R.drawable.ic_stats_fast,
            R.drawable.ic_stats_slow,
            R.drawable.ic_popup_current,
            R.drawable.ic_books)

    @Inject
    protected lateinit var bookDao: BookEntityDao

    private val compositeDisposable = CompositeDisposable()

    private val statsView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_stats, null, false)

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.label_stats)
                .setIcon(R.drawable.ic_stats)
                .setView(statsView)
                .setCancelable(true)
                .create()
    }

    override fun onResume() {
        super.onResume()
        loadIcons()
        showStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    /**
     * Vector drawables cannot be used as arguments for xml methods
     * like #drawableTop, therefore set it in code, in order to avoid
     * multiple vector files as a workaround
     */
    private fun loadIcons() {
        Single.fromCallable {
            drawableResList.mapTo(mutableListOf<Drawable>()) {
                AppCompatDrawableManager.get().getDrawable(context!!, it)
            }.toList()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { list ->
            list.forEachIndexed { index, drawable ->
                statsViews[index].setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            }
        }
    }

    private fun showStats() {
        compositeDisposable.add(bookDao.bookObservable.subscribe {
            compositeDisposable.add(DanteUtils.buildStatistics(it).subscribe { stats ->

                statsViews[0].text = getString(R.string.stats_pages_read, stats.pagesRead)
                statsViews[1].text = getString(R.string.stats_pages_waiting, stats.pagesWaiting)
                statsViews[2].text = getString(R.string.stats_books_read, stats.booksRead)
                statsViews[3].text = getString(R.string.stats_books_waiting, stats.booksWaiting)

                val textFastestBook = if (stats.fastestBook != null) {
                    getString(R.string.stats_duration_book,
                            stats.fastestBook.days, stats.fastestBook.bookName)
                } else {
                    getString(R.string.stats_book_duration_default)
                }
                statsViews[4].text = textFastestBook

                val textSlowestBook = if (stats.slowestBook != null) {
                    getString(R.string.stats_duration_book,
                            stats.slowestBook.days, stats.slowestBook.bookName)
                } else {
                    getString(R.string.stats_book_duration_default)
                }
                statsViews[5].text = textSlowestBook

                statsViews[6].text = getString(R.string.stats_avg_books_per_month,
                        stats.avgBooksPerMonth.toString())
                val textMostReadingMonth = if (stats.mostReadingMonth != null) {
                    getString(R.string.stats_most_reading_month,
                            stats.mostReadingMonth.finishedBooks, stats.mostReadingMonth.monthAsString)
                } else {
                    getString(R.string.stats_book_duration_default)
                }
                statsViews[7].text = textMostReadingMonth
            })
        })
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
