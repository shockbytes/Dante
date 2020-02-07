package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BareBoneBook

data class FavoriteAuthor(
    val author: String,
    val books: List<BareBoneBook>
)
