package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Sort

class RealmPageRecordDao(private val realm: RealmInstanceProvider) : PageRecordDao {

    private val mapper = RealmPageRecordMapper()

    private val pageRecordClass = RealmPageRecord::class.java

    override fun insertPageRecordForId(
            id: Long,
            fromPage: Int,
            toPage: Int,
            nowInMillis: Long
    ) {
        insert(
                PageRecord(
                        bookId = id,
                        fromPage = fromPage,
                        toPage = toPage,
                        timestamp = nowInMillis
                )
        )
    }

    override fun deletePageRecordForBook(pageRecord: PageRecord): Completable {
        return Completable.fromAction {
            realm.instance.executeTransaction { realm ->
                realm.where(RealmPageRecord::class.java)
                        .equalTo("bookId", pageRecord.bookId)
                        .and()
                        .equalTo("timestamp", pageRecord.timestamp)
                        .findFirst()
                        ?.deleteFromRealm()
            }
        }
    }

    private fun insert(pageRecord: PageRecord) {
        realm.instance.executeTransaction { realm ->
            realm.copyToRealm(mapper.mapFrom(pageRecord))
        }
    }

    override fun pageRecordsForBook(bookId: Long): Observable<List<PageRecord>> {
        return realm.instance.where(pageRecordClass)
                .equalTo("bookId", bookId)
                .sort("timestamp", Sort.ASCENDING)
                .findAllAsync()
                .asFlowable()
                .map(mapper::mapTo)
                .toObservable()
    }

    override fun allPageRecords(): Observable<List<PageRecord>> {
        return realm.instance.where(pageRecordClass)
                .sort("timestamp", Sort.ASCENDING)
                .findAllAsync()
                .asFlowable()
                .map(mapper::mapTo)
                .toObservable()
    }
}