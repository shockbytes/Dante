package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DefaultBookRepository(private val localBookDao: BookEntityDao) : BookRepository {

    private var selectedBookDao: BookEntityDao

    init {
        selectedBookDao = retrieveSelectedBookDao()
    }

    /**
     * Decide which book dao to choose...
     * Maybe it's not needed at all and a Warehouse is used instead.
     */
    private fun retrieveSelectedBookDao(): BookEntityDao {
        return localBookDao
    }

    override val bookObservable: Observable<List<BookEntity>>
        get() = selectedBookDao.bookObservable

    override val allBooks: List<BookEntity>
        get() = selectedBookDao.allBooks

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = selectedBookDao.bookLabelObservable

    override val booksCurrentlyReading: List<BookEntity>
        get() = selectedBookDao.booksCurrentlyReading

    override operator fun get(id: BookId): Single<BookEntity> {
        return selectedBookDao[id]
    }

    override fun create(entity: BookEntity): Completable {
        return selectedBookDao.create(entity)
    }

    override fun update(entity: BookEntity): Completable {
        return selectedBookDao.update(entity)
    }

    override fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable {
        return selectedBookDao.updateCurrentPage(bookId, currentPage)
    }

    override fun delete(id: BookId): Completable {
        return selectedBookDao.delete(id)
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        return selectedBookDao.search(query)
    }

    override fun restoreBackup(backupBooks: List<BookEntity>, strategy: RestoreStrategy): Completable {
        return selectedBookDao.restoreBackup(backupBooks, strategy)
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return selectedBookDao.createBookLabel(bookLabel)
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return selectedBookDao.deleteBookLabel(bookLabel)
    }
}