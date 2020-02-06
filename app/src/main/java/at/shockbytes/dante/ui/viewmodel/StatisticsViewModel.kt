package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.statistics.BookStatistics
import at.shockbytes.dante.core.book.statistics.StatisticsDisplayItem
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.stats.BookStatsBuilder
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.roundDouble
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val bookDao: BookEntityDao
) : BaseViewModel() {

    private val statisticsItems = MutableLiveData<List<BookStatsItem>>()

    fun requestStatistics() {
        bookDao.bookObservable
            .map(BookStatsBuilder::createFrom)
            .subscribe(statisticsItems::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun getStatistics(): LiveData<List<BookStatsItem>> = statisticsItems

    private fun statsToItems(s: BookStatistics): List<StatisticsDisplayItem> {

        val items = mutableListOf<StatisticsDisplayItem>()

        // Pages and books category
        items.add(
            StatisticsDisplayItem.StatisticsHeaderItem(
                R.string.statistics_header_pages,
                R.drawable.ic_pages
            )
        )

        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_pages_waiting,
                R.drawable.ic_pages,
                R.color.tabcolor_upcoming_dark,
                StatisticsDisplayItem.Align.START,
                listOf(s.pagesWaiting.toString())
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_pages_read,
                R.drawable.ic_pages_read,
                R.color.tabcolor_done_dark,
                StatisticsDisplayItem.Align.START,
                listOf(s.pagesRead.toString())
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_books_waiting,
                R.drawable.ic_tab_upcoming,
                R.color.tabcolor_upcoming,
                StatisticsDisplayItem.Align.START,
                listOf(s.booksWaiting.toString())
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_books_read,
                R.drawable.ic_tab_done,
                R.color.tabcolor_done,
                StatisticsDisplayItem.Align.START, listOf(s.booksRead.toString())
            )
        )

        // Time category
        items.add(
            StatisticsDisplayItem.StatisticsHeaderItem(
                R.string.statistics_header_time,
                R.drawable.ic_time
            )
        )

        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_duration_book,
                R.drawable.ic_stats_fast,
                R.color.colorAccent,
                StatisticsDisplayItem.Align.START,
                listOf(s.fastestBook?.days?.toString() ?: "---", s.fastestBook?.bookName ?: "---")
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_duration_book,
                R.drawable.ic_stats_slow,
                R.color.color_error,
                StatisticsDisplayItem.Align.START,
                listOf(
                    s.slowestBook?.days?.toString() ?: "---",
                    s.slowestBook?.bookName ?: "---"
                )
            )
        )

        // Favourites
        items.add(
            StatisticsDisplayItem.StatisticsHeaderItem(
                R.string.statistics_header_favs,
                R.drawable.ic_rating
            )
        )

        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_favourite_author,
                R.drawable.ic_fav_author,
                R.color.support_badge_standard,
                StatisticsDisplayItem.Align.START,
                listOf(s.mostReadAuthor ?: "---")
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_first_five_star,
                R.drawable.ic_rating,
                R.color.nice_color,
                StatisticsDisplayItem.Align.START,
                listOf(s.firstFiveStarBook?.title ?: "---")
            )
        )

        val avgRating = if (s.averageBookRating != 0.0) s.averageBookRating.roundDouble(2).toString() else "---"
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_average_rating,
                R.drawable.ic_rating_average,
                R.color.support_badge_premium,
                StatisticsDisplayItem.Align.START,
                listOf(avgRating)
            )
        )
        // Other book information
        items.add(
            StatisticsDisplayItem.StatisticsHeaderItem(
                R.string.statistics_header_other,
                R.drawable.ic_other
            )
        )

        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_avg_books_per_month,
                R.drawable.ic_tab_current,
                R.color.tabcolor_current,
                StatisticsDisplayItem.Align.START,
                listOf(s.avgBooksPerMonth.toString())
            )
        )
        items.add(
            StatisticsDisplayItem.StatisticsDataItem(
                R.string.statistics_most_reading_month,
                R.drawable.ic_books,
                R.color.brown,
                StatisticsDisplayItem.Align.START,
                listOf(
                    s.mostReadingMonth?.finishedBooks?.toString() ?: "0",
                    s.mostReadingMonth?.monthAsString ?: "---")
            )
        )

        return items
    }
}