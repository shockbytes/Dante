package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

class DefaultBookRepository(
    private val localBookDao: BookEntityDao,
    private val remoteBookDao: BookEntityDao
) : BookRepository {

    private var selectedBookDao: BookEntityDao

    init {
        selectedBookDao = retrieveSelectedBookDao()
    }

    // TODO Figure out how to switch those
    private fun retrieveSelectedBookDao(): BookEntityDao {
        return localBookDao
    }

    override val bookObservable: Observable<List<BookEntity>>
        get() = selectedBookDao.bookObservable

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = selectedBookDao.bookLabelObservable

    override val booksCurrentlyReading: List<BookEntity>
        get() = selectedBookDao.booksCurrentlyReading

    override val localBooksCount: Single<Int>
        get() = localBookDao.bookObservable
            .first(listOf())
            .map { it.count() }

    override operator fun get(id: Long): Maybe<BookEntity> {
        return selectedBookDao[id]
    }

    // TODO REACTIVE Make one caller reactive too
    override fun create(entity: BookEntity): Completable {
        return selectedBookDao.create(entity)
    }

    // TODO REACTIVE Make these calls reactive too!
    override fun update(entity: BookEntity): Completable {
        return selectedBookDao.update(entity)
    }

    // TODO REACTIVE Make these calls reactive too!
    override fun updateCurrentPage(bookId: Long, currentPage: Int): Completable {
        return selectedBookDao.updateCurrentPage(bookId, currentPage)
    }

    // TODO REACTIVE Make these calls reactive too!
    override fun delete(id: Long): Completable {
        return selectedBookDao.delete(id)
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        return selectedBookDao.search(query)
    }

    override fun restoreBackup(backupBooks: List<BookEntity>, strategy: RestoreStrategy): Completable {
        return selectedBookDao.restoreBackup(backupBooks, strategy)
    }

    override fun createBookLabel(bookLabel: BookLabel) {
        return selectedBookDao.createBookLabel(bookLabel)
    }

    override fun deleteBookLabel(bookLabel: BookLabel) {
        selectedBookDao.deleteBookLabel(bookLabel)
    }

    override fun migrateToRemoteStorage(): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}