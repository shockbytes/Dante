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

    fun create(entity: BookEntity)

    fun update(entity: BookEntity)

    fun updateCurrentPage(bookId: Long, currentPage: Int)

    fun delete(id: Long)

    fun search(query: String): Observable<List<BookEntity>>

    fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable

    fun createBookLabel(bookLabel: BookLabel)

    fun deleteBookLabel(bookLabel: BookLabel)

    fun migrateToRemoteStorage(): Completable
}