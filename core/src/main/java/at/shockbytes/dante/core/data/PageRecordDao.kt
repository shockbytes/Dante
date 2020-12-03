package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Observable

interface PageRecordDao {

    fun insertPageRecordForBookId(
        bookId: Long,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ): Completable

    fun updatePageRecord(pageRecord: PageRecord, fromPage: Int?, toPage: Int?): Completable

    fun deletePageRecordForBook(pageRecord: PageRecord): Completable

    fun deleteAllPageRecordsForBookId(bookId: Long): Completable

    fun pageRecordsForBook(bookId: Long): Observable<List<PageRecord>>

    fun allPageRecords(): Observable<List<PageRecord>>

    fun restoreBackup(pageRecords: List<PageRecord>, strategy: RestoreStrategy): Completable
}