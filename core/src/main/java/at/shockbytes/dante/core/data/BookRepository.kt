package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

interface BookRepository {

    val bookObservable: Observable<List<BookEntity>>

    val bookLabelObservable: Observable<List<BookLabel>>

    val booksCurrentlyReading: List<BookEntity>

    val localBooksCount: Single<Int>

    operator fun get(id: Long): Maybe<BookEntity>

    fun create(entity: BookEntity): Completable

    fun update(entity: BookEntity): Completable

    fun updateCurrentPage(bookId: Long, currentPage: Int): Completable

    fun delete(id: Long): Completable

    fun search(query: String): Observable<List<BookEntity>>

    fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable

    fun createBookLabel(bookLabel: BookLabel): Completable

    fun deleteBookLabel(bookLabel: BookLabel): Completable

    fun migrateToRemoteStorage(): Completable
}