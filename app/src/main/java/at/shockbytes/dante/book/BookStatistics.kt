package at.shockbytes.dante.book

import at.shockbytes.util.AppUtils
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Months

/**
 * @author Martin Macheiner
 * Date: 03.02.2018.
 */

data class BookStatistics(val pagesRead: Int, val pagesWaiting: Int,
                          val booksRead: Int, val booksWaiting: Int,
                          val fastestBook: Duration?, val slowestBook: Duration?,
                          val avgBooksPerMonth: Double, val mostReadingMonth: MostReadingMonth?) {

    data class Duration(val bookName: String, val days: Long)

    data class MostReadingMonth(val monthAsString: String, val finishedBooks: Int)

    companion object {

        fun averageBooksPerMonth(booksDone: List<BookEntity>): Double {

            val now = System.currentTimeMillis()
            val start = booksDone.map { it.startDate }.sorted().firstOrNull() ?: now
            val monthsWhileReading = Months.monthsBetween(DateTime(start), DateTime(now)).months

            return if (monthsWhileReading == 0) {
                booksDone.size.toDouble()
            } else {
                AppUtils.roundDouble(booksDone.size / monthsWhileReading.toDouble(), 2)
            }
        }

        fun bookDurations(booksDone: List<BookEntity>): Pair<Duration?, Duration?> {

            val durations = booksDone
                    .asSequence()
                    .map { it ->
                        var days = Duration(it.endDate - it.startDate).standardDays
                        if (days == 0L) {
                            days = 1
                        }
                        Duration(it.title, days)
                    }
                    .sortedBy { it.days }
            return Pair(durations.firstOrNull(), durations.lastOrNull())
        }

        fun mostReadingMonth(booksDone: List<BookEntity>): MostReadingMonth? {

            val maxMonth = booksDone
                    .asSequence()
                    .map { DateTime(it.endDate).monthOfYear() }
                    .groupBy { it }
                    .maxBy { it.value.size }

            return if (maxMonth != null) {
                val d = maxMonth.key.dateTime
                MostReadingMonth(d.toString("MMM yyyy"), maxMonth.value.size)
            } else {
                null
            }
        }

        fun from(books: List<BookEntity>): Single<BookStatistics> {
            return Single.fromCallable {

                val upcoming = books.filter { it.state == BookState.READ_LATER }
                val done = books.filter { it.state == BookState.READ }
                val reading = books.filter { it.state == BookState.READING }

                // Add pages in the currently read book to read pages
                val pagesRead = done.sumBy { it.pageCount } + reading.sumBy { it.currentPage }
                // Add pages waiting in the current book to waiting pages
                val pagesWaiting = upcoming.sumBy { it.pageCount } + reading.sumBy { it.pageCount - it.currentPage }
                val (fastestBook, slowestBook) = BookStatistics.bookDurations(done)
                val avgBooksPerMonth = BookStatistics.averageBooksPerMonth(done)
                val mostReadingMonth = BookStatistics.mostReadingMonth(done)

                BookStatistics(pagesRead, pagesWaiting,
                        done.size, upcoming.size,
                        fastestBook, slowestBook,
                        avgBooksPerMonth, mostReadingMonth)
            }
        }

    }

}