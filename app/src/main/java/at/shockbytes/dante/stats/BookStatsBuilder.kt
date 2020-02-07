package at.shockbytes.dante.stats

import at.shockbytes.dante.core.bareBone
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import org.joda.time.Duration

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
        // TODO
        return BookStatsItem.BooksAndPages
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
        // TODO
        return BookStatsItem.Others
    }
}