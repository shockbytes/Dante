package at.shockbytes.dante.core.data.warehouse

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.warehouse.Warehouse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class WarehouseBookEntityDao(
    private val warehouse: Warehouse<BookEntity>
) : BookEntityDao {

    override val bookObservable: Observable<List<BookEntity>>
        get() = warehouse.getAll()

    override val booksCurrentlyReading: List<BookEntity>
        get() {
            // TODO
            return listOf()
        }

    override fun get(id: BookId): Single<BookEntity> {
        TODO("Not yet implemented")
    }

    override fun create(entity: BookEntity): Completable {
        return warehouse.store(entity)
    }

    override fun update(entity: BookEntity): Completable {
        return warehouse.update(entity)
    }

    override fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable {
        // TODO
        return Completable.complete()
    }

    override fun delete(id: BookId): Completable {
        return get(id).flatMapCompletable { book ->
            warehouse.delete(book)
        }
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
}