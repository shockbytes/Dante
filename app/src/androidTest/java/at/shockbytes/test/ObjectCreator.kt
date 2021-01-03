package at.shockbytes.test

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId

object ObjectCreator {

    fun getPopulatedListOfBookEntities(): List<BookEntity> {
        return listOf(
            BookEntity(id = BookId(0), title = "Their darkest hour"),
            BookEntity(id = BookId(1), title = "Homo Faber"),
            BookEntity(id = BookId(3), title = "The ego is the enemy")
        )
    }
}