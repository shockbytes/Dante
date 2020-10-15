package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.Sort

class RealmPageRecordDao(private val realm: RealmInstanceProvider) : PageRecordDao {

    private val mapper = RealmPageRecordMapper()

    private val pageRecordClass = RealmPageRecord::class.java

    override fun insertPageRecordForBookId(
        bookId: Long,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ) {
        insert(
            PageRecord(
                bookId = bookId,
                fromPage = fromPage,
                toPage = toPage,
                timestamp = nowInMillis
            )
        )
    }

    override fun updatePageRecord(
        pageRecord: PageRecord,
        fromPage: Int?,
        toPage: Int?
    ): Completable {
        return Completable.fromAction {
            realm.instance.executeTransaction { realm ->
                realm.findPageRecord(pageRecord)?.let { realmRecord ->

                    fromPage?.let { realmRecord.fromPage = fromPage }
                    toPage?.let { realmRecord.toPage = toPage }

                    realm.copyToRealmOrUpdate(realmRecord)
                }
            }
        }
    }

    override fun deletePageRecordForBook(pageRecord: PageRecord): Completable {
        return Completable.fromAction {
            realm.instance.executeTransaction { realm ->
                realm.findPageRecord(pageRecord)?.deleteFromRealm()
            }
        }
    }

    private fun Realm.findPageRecord(pageRecord: PageRecord): RealmPageRecord? {
        return where(pageRecordClass)
            .equalTo("bookId", pageRecord.bookId)
            .and()
            .equalTo("timestamp", pageRecord.timestamp)
            .findFirst()
    }

    override fun deleteAllPageRecordsForBookId(bookId: Long): Completable {
        return Completable.fromAction {
            realm.instance.executeTransaction { realm ->
                realm.where(pageRecordClass)
                    .equalTo("bookId", bookId)
                    .findAll()
                    ?.deleteAllFromRealm()
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