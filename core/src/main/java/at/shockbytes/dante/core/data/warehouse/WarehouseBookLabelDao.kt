package at.shockbytes.dante.core.data.warehouse

import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookLabelDao
import at.shockbytes.warehouse.Warehouse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class WarehouseBookLabelDao(
    private val warehouse: Warehouse<BookLabel>
): BookLabelDao {

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = warehouse.getAll()

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return warehouse.store(bookLabel)
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return warehouse.delete(bookLabel)
    }
}