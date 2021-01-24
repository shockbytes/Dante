package at.shockbytes.test

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId

object ObjectCreator {

    fun getPopulatedListOfBookEntities(): List<BookEntity> {
        return listOf(
            BookEntity(id = 0L, title = "Their darkest hour"),
            BookEntity(id = 1L, title = "Homo Faber"),
            BookEntity(id = 3L, title = "The ego is the enemy")
        )
    }
}