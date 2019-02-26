package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.book.statistics.BookStatistics
import at.shockbytes.dante.book.statistics.StatisticsDisplayItem
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.roundDouble
import timber.log.Timber
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(private val bookDao: BookEntityDao) : BaseViewModel() {

    private val statisticsItems = MutableLiveData<List<StatisticsDisplayItem>>()

    fun requestStatistics() {
        bookDao.bookObservable
                .flatMapSingle { books ->
                    BookStatistics.from(books)
                            .map { statsToItems(it) }
                }
                .subscribe({ items ->
                    statisticsItems.postValue(items)
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)
    }

    fun getStatistics(): LiveData<List<StatisticsDisplayItem>> = statisticsItems

    private fun statsToItems(s: BookStatistics): List<StatisticsDisplayItem> {

        val items = mutableListOf<StatisticsDisplayItem>()

        // Pages and books category
        items.add(StatisticsDisplayItem.StatisticsHeaderItem(R.string.statistics_header_pages, R.drawable.ic_pages))

        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_pages_waiting,
                R.drawable.ic_pages_colored, StatisticsDisplayItem.Align.START, s.pagesWaiting.toString()))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_pages_read,
                R.drawable.ic_pages, StatisticsDisplayItem.Align.START, s.pagesRead.toString()))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_books_waiting,
                R.drawable.ic_popup_upcoming, StatisticsDisplayItem.Align.START, s.booksWaiting.toString()))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_books_read,
                R.drawable.ic_popup_done, StatisticsDisplayItem.Align.START, s.booksRead.toString()))

        // Time category
        items.add(StatisticsDisplayItem.StatisticsHeaderItem(R.string.statistics_header_time, R.drawable.ic_time))

        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_duration_book,
                R.drawable.ic_stats_fast, StatisticsDisplayItem.Align.START,
                s.fastestBook?.days?.toString() ?: "---", s.fastestBook?.bookName ?: "---"))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_duration_book,
                R.drawable.ic_stats_slow, StatisticsDisplayItem.Align.START,
                s.slowestBook?.days?.toString() ?: "---", s.slowestBook?.bookName ?: "---"))

        // Favourites
        items.add(StatisticsDisplayItem.StatisticsHeaderItem(R.string.statistics_header_favs, R.drawable.ic_rating))

        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_favourite_author,
                R.drawable.ic_fav_author, StatisticsDisplayItem.Align.START, s.mostReadAuthor ?: "---"))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_first_five_star,
                R.drawable.ic_five_star, StatisticsDisplayItem.Align.START, s.firstFiveStarBook?.title ?: "---"))

        val avgRating = if (s.averageBookRating != 0.0) s.averageBookRating.roundDouble(2).toString() else "---"
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_average_rating,
                R.drawable.ic_rating_colored, StatisticsDisplayItem.Align.START, avgRating))
        // Other book information
        items.add(StatisticsDisplayItem.StatisticsHeaderItem(R.string.statistics_header_other, R.drawable.ic_other))

        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_avg_books_per_month,
                R.drawable.ic_popup_current, StatisticsDisplayItem.Align.START, s.avgBooksPerMonth.toString()))
        items.add(StatisticsDisplayItem.StatisticsDataItem(R.string.statistics_most_reading_month,
                R.drawable.ic_books, StatisticsDisplayItem.Align.START,
                s.mostReadingMonth?.finishedBooks?.toString() ?: "0",
                s.mostReadingMonth?.monthAsString ?: "---"))

        return items
    }
}