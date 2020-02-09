package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BareBoneBook

data class MostActiveMonth(
    val monthAsString: String,
    val books: List<BareBoneBook>
) {

    val finishedBooks: Int
        get() = books.size
}
