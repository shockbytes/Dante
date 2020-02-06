package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BookEntity

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
        return BookStatsItem.BooksAndPages
    }

    private fun createReadingDurationItem(books: List<BookEntity>): BookStatsItem {
        return BookStatsItem.ReadingDuration
    }

    private fun createFavoriteItem(books: List<BookEntity>): BookStatsItem {
        return BookStatsItem.Favorites
    }

    private fun createLanguageItem(books: List<BookEntity>): BookStatsItem {
        return BookStatsItem.Languages
    }

    private fun createOthersItem(books: List<BookEntity>): BookStatsItem {
        return BookStatsItem.Others
    }
}