package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookLabel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class DefaultBookLabelRepository(
    private val labelDao: BookLabelDao
) : BookLabelRepository {

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = labelDao.bookLabelObservable

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return labelDao.createBookLabel(bookLabel)
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return labelDao.deleteBookLabel(bookLabel)
    }
}