package at.shockbytes.dante.stats

import at.shockbytes.dante.core.bareBone
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.util.AppUtils
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Months

object BookStatsBuilder {

    fun createFrom(books: List<BookEntity>): List<BookStatsItem> {
        return listOf(
            createBooksAndPagesItem(books),
            createReadingDurationItem(books),
            createFavoriteItem(books),
            createLanguageItem(books),
            createOthersItem(books)
        )
    }

    private fun createBooksAndPagesItem(books: List<BookEntity>): BookStatsItem {

        if (books.isEmpty()) {
            return BookStatsItem.BooksAndPages.Empty
        }

        val states = books.groupBy { it.state }

        val waiting = states[BookState.READ_LATER] ?: listOf()
        val read = states[BookState.READ] ?: listOf()
        val reading = states[BookState.READING] ?: listOf()

        // Add pages in the currently read book to read pages
        val pagesRead = read.sumBy { it.pageCount } + reading.sumBy { it.currentPage }
        // Add pages waiting in the current book to waiting pages
        val pagesWaiting = waiting.sumBy { it.pageCount } + reading.sumBy { it.pageCount - it.currentPage }

        return BookStatsItem.BooksAndPages.Present(
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

    private fun createReadingDurationItem(books: List<BookEntity>): BookStatsItem {

            val booksWithDuration = books
                .asSequence()
                .filter { it.startDate > 0 } // Only take books where the start date is set
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
            BookStatsItem.ReadingDuration.Present(
                slowest = booksWithDuration.last(),
                fastest = booksWithDuration.first()
            )
        } else {
            BookStatsItem.ReadingDuration.Empty
        }
    }

    private fun createFavoriteItem(books: List<BookEntity>): BookStatsItem {

        val firstFiveStar = firstFiveStarBook(books)
        val favoriteAuthor = favoriteAuthor(books)

        return if (firstFiveStar != null && favoriteAuthor != null) {
            BookStatsItem.Favorites.Present(favoriteAuthor, firstFiveStar)
        } else {
            BookStatsItem.Favorites.Empty
        }
    }

    private fun favoriteAuthor(books: List<BookEntity>): FavoriteAuthor? {
        return books
            .asSequence()
            .filter { book ->
                book.state == BookState.READ
            }
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

    private fun createLanguageItem(books: List<BookEntity>): BookStatsItem {

        val languages = books.asSequence()
            .filter { it.language != null }
            .groupBy { it.language!! }
            .mapValues { it.value.size }

        return BookStatsItem.Languages(languages)
    }

    private fun createOthersItem(books: List<BookEntity>): BookStatsItem {

        if (books.isEmpty()) {
            BookStatsItem.ReadingDuration.Empty
        }

        return BookStatsItem.Others.Present(
            averageRating = averageBookRating(books),
            averageBooksPerMonth = averageBooksPerMonth(books),
            mostActiveMonth = mostActiveMonth(books)
        )
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

    private fun averageBooksPerMonth(booksDone: List<BookEntity>): Double {

        val now = System.currentTimeMillis()
        val start = booksDone
            .filter { it.startDate > 0 && it.state == BookState.READ }
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