package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookLabel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface BookLabelDao {

    val bookLabelObservable: Observable<List<BookLabel>>

    fun createBookLabel(bookLabel: BookLabel): Completable

    fun deleteBookLabel(bookLabel: BookLabel): Completable
}