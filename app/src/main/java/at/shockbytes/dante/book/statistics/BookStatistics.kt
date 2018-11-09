package at.shockbytes.dante.book.statistics

import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.util.AppUtils
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Months

/**
 * @author  Martin Macheiner
 * Date:    03.02.2018
 */
data class BookStatistics(val pagesRead: Int,                           // Pages & books
                          val pagesWaiting: Int,                        // Pages & books
                          val booksRead: Int,                           // Pages & books
                          val booksWaiting: Int,                        // Pages & books
                          val fastestBook: Duration?,                   // Time
                          val slowestBook: Duration?,                   // Time
                          val avgBooksPerMonth: Double,                 // Other
                          val mostReadingMonth: MostReadingMonth?,      // Other
                          val mostReadAuthor: String?,                  // Favourites
                          val averageBookRating: Double,                // Other
                          val firstFiveStarBook: StatsBookDisplayItem?  // Favourites
) {

    // TODO Fix calculations

    data class Duration(val bookName: String, val days: Long)

    data class MostReadingMonth(val monthAsString: String, val finishedBooks: Int)

    data class StatsBookDisplayItem(val title: String, val thumbUrl: String?)

    companion object {

        private fun averageBooksPerMonth(booksDone: List<BookEntity>): Double {

            val now = System.currentTimeMillis()
            val start = booksDone.map { it.startDate }.sorted().firstOrNull() ?: now
            val monthsWhileReading = Months.monthsBetween(DateTime(start), DateTime(now)).months

            return if (monthsWhileReading == 0) {
                booksDone.size.toDouble()
            } else {
                AppUtils.roundDouble(booksDone.size / monthsWhileReading.toDouble(), 2)
            }
        }

        private fun bookDurations(booksDone: List<BookEntity>): Pair<Duration?, Duration?> {
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

        private fun mostReadingMonth(booksDone: List<BookEntity>): MostReadingMonth? {
            val maxMonth = booksDone
                    .asSequence()
                    .map { DateTime(it.endDate).monthOfYear() }
                    .groupBy { it }
                    .maxBy { it.value.size }

            return if (maxMonth != null) {
                val d = maxMonth.key.dateTime
                MostReadingMonth(d.toString("MMM yyyy"), maxMonth.value.size)
            } else null
        }

        private fun averageBookRating(books: List<BookEntity>): Double {
            val booksWithRating = books.filter { it.rating > 0 }
            return if (booksWithRating.isNotEmpty()) {
                booksWithRating
                        .map { it.rating.toDouble() }
                        .average()
            } else 0.0
        }

        private fun mostReadAuthor(done: List<BookEntity>): String? {
            return done
                    .asSequence()
                    .map { it.author }
                    .groupBy { it }
                    .maxBy { it.value.size }
                    ?.key
        }

        private fun firstFiveStar(books: List<BookEntity>): StatsBookDisplayItem? {
            return books
                    .asSequence()
                    .filter { it.rating == 5 }
                    .minBy { it.endDate }
                    ?.let { StatsBookDisplayItem(it.title, it.thumbnailAddress) }
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
                val (fastestBook, slowestBook) = bookDurations(done)
                val avgBooksPerMonth = averageBooksPerMonth(done)
                val mostReadingMonth = mostReadingMonth(done)
                val averageBookRating = averageBookRating(books)
                val mostReadAuthor = mostReadAuthor(done)
                val firstFiveStarBook = firstFiveStar(books)

                BookStatistics(pagesRead,
                        pagesWaiting,
                        done.size,
                        upcoming.size,
                        fastestBook,
                        slowestBook,
                        avgBooksPerMonth,
                        mostReadingMonth,
                        mostReadAuthor,
                        averageBookRating,
                        firstFiveStarBook)
            }
        }

    }

}