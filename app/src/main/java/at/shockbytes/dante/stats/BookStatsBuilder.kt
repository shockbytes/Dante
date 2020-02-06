package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BookEntity

object BookStatsBuilder {

    // TODO
    fun createFrom(booksObservable: List<BookEntity>): List<BookStatsItem> {
        return listOf(BookStatsItem())
    }
}