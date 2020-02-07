package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BareBoneBook

data class BookWithDuration(
    val book: BareBoneBook,
    val days: Int
)
