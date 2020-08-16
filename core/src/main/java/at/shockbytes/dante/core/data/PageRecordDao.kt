package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.PageRecord
import io.reactivex.Completable
import io.reactivex.Observable

interface PageRecordDao {

    fun insertPageRecordForId(
            id: Long,
            fromPage: Int,
            toPage: Int,
            nowInMillis: Long
    )

    fun pageRecordsForBook(bookId: Long): Observable<List<PageRecord>>

    fun allPageRecords(): Observable<List<PageRecord>>
}