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

    override fun create(entity: BookEntity): Completable {
        return selectedBookDao.create(entity)
    }

    override fun update(entity: BookEntity) {
        selectedBookDao.update(entity)
    }

    override fun updateCurrentPage(bookId: Long, currentPage: Int) {
        selectedBookDao.updateCurrentPage(bookId, currentPage)
    }

    override fun delete(id: Long) {
        selectedBookDao.delete(id)
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