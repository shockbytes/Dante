package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.util.RestoreStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface PageRecordDao {

    fun insertPageRecordForBookId(
        bookId: BookId,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ): Completable

    fun updatePageRecord(pageRecord: PageRecord, fromPage: Int?, toPage: Int?): Completable

    fun deletePageRecordForBook(pageRecord: PageRecord): Completable

    fun deleteAllPageRecordsForBookId(bookId: BookId): Completable

    fun pageRecordsForBook(bookId: BookId): Observable<List<PageRecord>>

    fun allPageRecords(): Observable<List<PageRecord>>

    fun restoreBackup(pageRecords: List<PageRecord>, strategy: RestoreStrategy): Completable
}