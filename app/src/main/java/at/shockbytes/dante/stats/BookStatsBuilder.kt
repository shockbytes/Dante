package at.shockbytes.dante.stats

import android.graphics.Color
import at.shockbytes.dante.core.bareBone
import at.shockbytes.dante.core.book.*
import at.shockbytes.dante.ui.adapter.stats.model.LabelStatsItem
import at.shockbytes.dante.ui.custom.pages.PageRecordDataPoint
import at.shockbytes.util.AppUtils
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Months
import org.joda.time.format.DateTimeFormat

object BookStatsBuilder {

    fun createFrom(
            books: List<BookEntity>,
            pageRecords: List<PageRecord>,
            pagesPerMonthGoal: ReadingGoal.PagesPerMonthReadingGoal
    ): List<BookStatsViewItem> {
        return mutableListOf(
            createBooksAndPagesItem(books),
            createPagesOverTimeItem(pageRecords, pagesPerMonthGoal),
            createReadingDurationItem(books),
            createFavoriteItem(books),
            createLanguageItem(books),
            createLabelItem(books),
            createOthersItem(books)
        )
    }

    private fun createBooksAndPagesItem(books: List<BookEntity>): BookStatsViewItem {

        if (books.isEmpty()) {
            return BookStatsViewItem.BooksAndPages.Empty
        }

        val states = books.groupBy { it.state }

        val waiting = states[BookState.READ_LATER] ?: listOf()
        val read = states[BookState.READ] ?: listOf()
        val reading = states[BookState.READING] ?: listOf()

        // Add pages in the currently read book to read pages
        val pagesRead = read.sumBy { it.pageCount } + reading.sumBy { it.currentPage }
        // Add pages waiting in the current book to waiting pages
        val pagesWaiting = waiting.sumBy { it.pageCount } + reading.sumBy { it.pageCount - it.currentPage }

        return BookStatsViewItem.BooksAndPages.Present(
            BooksPagesInfo(
                books = BooksPagesInfo.Books(
                    waiting = waiting.size,
                    reading = reading.size,
                    read = read.size
                ),
                pages = BooksPagesInfo.Pages(
                    waiting = pagesWaiting,
                    read = pagesRead
                )
            )
        )
    }

    private fun createPagesOverTimeItem(
            pageRecords: List<PageRecord>,
            pagesPerMonthGoal: ReadingGoal.PagesPerMonthReadingGoal
    ): BookStatsViewItem {

        if (pageRecords.isEmpty()) {
            return BookStatsViewItem.PagesOverTime.Empty
        }
        val format = DateTimeFormat.forPattern("MMM yy")

        return pageRecords
                .groupBy { record ->
                    val dt = record.dateTime
                    MonthYear(dt.monthOfYear, dt.year)
                }
                .toSortedMap()
                .map { (monthYear, records) ->

                    val pages = records
                            .sumBy { it.diffPages }
                            // There can be negative values, hard bounce them at 0
                            // Example: User logs 100 pages in July but deletes 20 pages in August
                            // which leads to a value of -20. This should not happen!
                            .coerceAtLeast(0)

                    PageRecordDataPoint(pages, formattedDate = format.print(monthYear.dateTime))
                }
                .let {pageRecordDataPoints ->
                    BookStatsViewItem.PagesOverTime.Present(pageRecordDataPoints, pagesPerMonthGoal)
                }
    }


    private fun createReadingDurationItem(books: List<BookEntity>): BookStatsViewItem {

        val booksWithDuration = books
            .asSequence()
            .filter { it.startDate > 0 && it.state == BookState.READ } // Only take books where the start date is set
            .map { book ->
                val days = Duration(book.endDate - book.startDate)
                    .standardDays
                    .coerceAtLeast(1)
                    .toInt()
                BookWithDuration(book.bareBone(), days)
            }
            .sortedBy { it.days }
            .toList()

        return if (booksWithDuration.isNotEmpty()) {
            BookStatsViewItem.ReadingDuration.Present(
                slowest = booksWithDuration.last(),
                fastest = booksWithDuration.first()
            )
        } else {
            BookStatsViewItem.ReadingDuration.Empty
        }
    }

    private fun createFavoriteItem(books: List<BookEntity>): BookStatsViewItem {

        val firstFiveStar = firstFiveStarBook(books)
        val favoriteAuthor = favoriteAuthor(books)

        return if (favoriteAuthor != null) {
            BookStatsViewItem.Favorites.Present(favoriteAuthor, firstFiveStar)
        } else {
            BookStatsViewItem.Favorites.Empty
        }
    }

    private fun favoriteAuthor(books: List<BookEntity>): FavoriteAuthor? {
        return books
            .groupBy { book ->
                book.author
            }
            .maxBy { it.value.size }
            ?.let { (author, books) ->
                FavoriteAuthor(author, books.map { it.bareBone() })
            }
    }

    private fun firstFiveStarBook(books: List<BookEntity>): BareBoneBook? {
        return books
            .asSequence()
            .filter { book ->
                book.rating == 5 && book.startDate > 0
            }
            .minBy { book ->
                book.startDate
            }
            ?.bareBone()
    }

    private fun createLanguageItem(books: List<BookEntity>): BookStatsViewItem {

        val languages = books.asSequence()
            .filter { it.language != null }
            .groupBy { it.language!! }
            .mapKeys { Languages.fromLanguageCode(it.key) }
            .mapValues { it.value.size }

        return if (languages.isEmpty()) {
            BookStatsViewItem.LanguageDistribution.Empty
        } else {
            BookStatsViewItem.LanguageDistribution.Present(languages)
        }
    }

    private fun createLabelItem(books: List<BookEntity>): BookStatsViewItem {

        val labels = books.asSequence()
            .map { it.labels }
            .flatten()
            .groupBy { LabelStatsItem(it.title, Color.parseColor(it.hexColor)) }
            .mapValues { it.value.size }

        return if (labels.isEmpty()) {
            BookStatsViewItem.LabelStats.Empty
        } else {
            BookStatsViewItem.LabelStats.Present(labels)
        }
    }

    private fun createOthersItem(books: List<BookEntity>): BookStatsViewItem {

        return if (books.isEmpty()) {
            BookStatsViewItem.Others.Empty
        } else {
            BookStatsViewItem.Others.Present(
                averageRating = averageBookRating(books),
                averageBooksPerMonth = averageBooksPerMonth(books),
                mostActiveMonth = mostActiveMonth(books)
            )
        }
    }

    private fun mostActiveMonth(books: List<BookEntity>): MostActiveMonth? {
        return books
            .asSequence()
            .filter { it.state == BookState.READ }
            .map { Pair(it.bareBone(), DateTime(it.endDate)) }
            .groupBy { it.second.monthOfYear * it.second.year }
            .maxBy { it.value.size }
            ?.let { max ->

                val activeBooks = max.value.map { it.first }
                val monthAsString = max.value.first().second.toString("MMM yyyy")

                MostActiveMonth(
                    monthAsString = monthAsString,
                    books = activeBooks
                )
            }
    }

    private fun averageBookRating(books: List<BookEntity>): Double {
        val booksWithRating = books.filter { it.rating > 0 }
        return if (booksWithRating.isNotEmpty()) {
            booksWithRating
                .map { it.rating.toDouble() }
                .average()
        } else 0.0
    }

    private fun averageBooksPerMonth(books: List<BookEntity>): Double {

        val booksDone = books.filter { it.startDate > 0 && it.state == BookState.READ }

        val now = System.currentTimeMillis()
        val start = booksDone
            .map { it.startDate }
            .min() ?: now
        val monthsReading = Months.monthsBetween(DateTime(start), DateTime(now)).months

        return if (monthsReading == 0) {
            booksDone.size.toDouble()
        } else {
            AppUtils.roundDouble(booksDone.size / monthsReading.toDouble(), 2)
        }
    }
}