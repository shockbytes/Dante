package at.shockbytes.dante.core.data.remote

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

class FirebaseBookDao : BookEntityDao {

    override val bookObservable: Observable<List<BookEntity>>
        get() = TODO("Not yet implemented")
    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = TODO("Not yet implemented")
    override val booksCurrentlyReading: List<BookEntity>
        get() = TODO("Not yet implemented")

    override fun get(id: Long): Maybe<BookEntity> {
        TODO("Not yet implemented")
    }

    override fun create(entity: BookEntity): Completable {
        TODO("Not yet implemented")
    }

    override fun update(entity: BookEntity): Completable {
        TODO("Not yet implemented")
    }

    override fun updateCurrentPage(bookId: Long, currentPage: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long): Completable {
        TODO("Not yet implemented")
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        TODO("Not yet implemented")
    }

    override fun restoreBackup(backupBooks: List<BookEntity>, strategy: RestoreStrategy): Completable {
        TODO("Not yet implemented")
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        TODO("Not yet implemented")
    }
}