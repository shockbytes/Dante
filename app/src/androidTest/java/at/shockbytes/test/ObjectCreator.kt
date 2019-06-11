package at.shockbytes.test

import at.shockbytes.dante.book.BookEntity

object ObjectCreator {



    fun getPopulatedListOfBookEntities(): List<BookEntity> {
        return listOf(
            BookEntity(id = 0, title = "Their darkest hour"),
            BookEntity(id = 1, title = "Homo Faber"),
            BookEntity(id = 3, title = "The ego is the enemy")
        )
    }
}