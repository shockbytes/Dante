package at.shockbytes.dante.data

import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.book.realm.RealmBook
import io.realm.RealmList

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityMapper : Mapper<RealmBook, BookEntity>() {

    override fun mapFrom(data: BookEntity): RealmBook {
        val book = RealmBook(data.id, data.title, data.subTitle, data.author,
                data.pageCount, data.publishedDate, data.position, data.isbn,
                data.thumbnailAddress, data.googleBooksLink,
                data.startDate, data.endDate, data.wishlistDate, data.language ?: "NA",
                data.rating, data.currentPage, data.notes, data.summary, RealmList(*data.labels.toTypedArray()))
        book.state = RealmBook.State.values()[data.state.ordinal]
        return book
    }

    override fun mapTo(data: RealmBook): BookEntity {
        return BookEntity(data.id, data.title, data.subTitle, data.author,
                BookState.values()[data.state.ordinal], data.pageCount, data.publishedDate, data.position, data.isbn,
                data.thumbnailAddress, data.googleBooksLink,
                data.startDate, data.endDate, data.wishlistDate, data.language,
                data.rating, data.currentPage, data.notes, data.summary, data.labels.toList())
    }
}