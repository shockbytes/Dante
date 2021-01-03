package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
interface BookEntityDao {

    val bookObservable: Observable<List<BookEntity>>

    val bookLabelObservable: Observable<List<BookLabel>>

    val booksCurrentlyReading: List<BookEntity>

    operator fun get(id: BookId): Single<BookEntity>

    fun create(entity: BookEntity): Completable

    fun update(entity: BookEntity): Completable

    fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable

    fun delete(id: BookId): Completable

    fun search(query: String): Observable<List<BookEntity>>

    fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable

    fun createBookLabel(bookLabel: BookLabel): Completable

    fun deleteBookLabel(bookLabel: BookLabel): Completable
}