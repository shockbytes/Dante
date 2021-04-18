package at.shockbytes.dante.core.data.warehouse

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.warehouse.Warehouse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class WarehouseBookRepository(
    private val warehouse: Warehouse<BookEntity>
) : BookRepository {

    override val bookObservable: Observable<List<BookEntity>>
        get() {
            // TODO
            return Observable.just(listOf())
        }

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() {
            // TODO
            return Observable.just(listOf())
        }
    override val booksCurrentlyReading: List<BookEntity>
        get() {
            // TODO
            return listOf()
        }

    override fun get(id: BookId): Single<BookEntity> {
        TODO("Not yet implemented")
    }

    override fun create(entity: BookEntity): Completable {
        // TODO
        return Completable.complete()
    }

    override fun update(entity: BookEntity): Completable {
        // TODO
        return Completable.complete()
    }

    override fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable {
        // TODO
        return Completable.complete()
    }

    override fun delete(id: BookId): Completable {
        // TODO
        return Completable.complete()
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        // TODO
        return Observable.just(listOf())
    }

    override fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable {
        // TODO
        return Completable.complete()
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        // TODO
        return Completable.complete()
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        // TODO
        return Completable.complete()
    }
}