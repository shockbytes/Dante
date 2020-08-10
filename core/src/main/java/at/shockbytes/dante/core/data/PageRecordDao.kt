package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.realm.PageRecord
import io.reactivex.Observable

interface PageRecordDao {

    fun pageRecordsForBook(bookId: Long): Observable<List<PageRecord>>
}