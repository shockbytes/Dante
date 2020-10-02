package at.shockbytes.dante.core.data.remote

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Observable

class FirebaseBookDao : BookEntityDao {

    override val bookObservable: Observable<List<BookEntity>>
        get() = TODO("not implemented") // To change initializer of created properties use File | Settings | File Templates.
    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = TODO("not implemented") // To change initializer of created properties use File | Settings | File Templates.
    override val booksCurrentlyReading: List<BookEntity>
        get() = TODO("not implemented") // To change initializer of created properties use File | Settings | File Templates.

    override fun get(id: Long): BookEntity? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun create(entity: BookEntity) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun update(entity: BookEntity) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCurrentPage(bookId: Long, currentPage: Int) {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun restoreBackup(backupBooks: List<BookEntity>, strategy: RestoreStrategy): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun createBookLabel(bookLabel: BookLabel) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteBookLabel(bookLabel: BookLabel) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}