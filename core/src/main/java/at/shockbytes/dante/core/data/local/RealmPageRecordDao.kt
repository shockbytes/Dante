package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.util.completableOf
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.realm.Realm
import io.realm.Sort

class RealmPageRecordDao(private val realm: RealmInstanceProvider) : PageRecordDao {

    private val mapper = RealmPageRecordMapper()

    private val pageRecordClass = RealmPageRecord::class.java

    override fun insertPageRecordForBookId(
        bookId: BookId,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ): Completable {
        return completableOf {
            insert(
                PageRecord(
                    bookId = bookId,
                    fromPage = fromPage,
                    toPage = toPage,
                    timestamp = nowInMillis
                )
            )
        }
    }

    override fun updatePageRecord(
        pageRecord: PageRecord,
        fromPage: Int?,
        toPage: Int?
    ): Completable {
        return Completable.fromAction {
            realm.executeTransaction { realm ->
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
            realm.executeTransaction { realm ->
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

    override fun deleteAllPageRecordsForBookId(bookId: BookId): Completable {
        return Completable.fromAction {
            realm.executeTransaction { realm ->
                realm.where(pageRecordClass)
                    .equalTo("bookId", bookId)
                    .findAll()
                    ?.deleteAllFromRealm()
            }
        }
    }

    private fun insert(pageRecord: PageRecord) {
        realm.executeTransaction { realm ->
            realm.copyToRealm(mapper.mapFrom(pageRecord))
        }
    }

    override fun pageRecordsForBook(bookId: BookId): Observable<List<PageRecord>> {
        return realm.read<RealmPageRecord>()
            .equalTo("bookId", bookId)
            .sort("timestamp", Sort.ASCENDING)
            .findAllAsync()
            .asFlowable()
            .map(mapper::mapTo)
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)
    }

    override fun allPageRecords(): Observable<List<PageRecord>> {
        return realm.read<RealmPageRecord>()
            .sort("timestamp", Sort.ASCENDING)
            .findAllAsync()
            .asFlowable()
            .map(mapper::mapTo)
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)
    }

    override fun restoreBackup(pageRecords: List<PageRecord>, strategy: RestoreStrategy): Completable {

        val preRestorationAction = when (strategy) {
            RestoreStrategy.MERGE -> Completable.complete() // No previous action
            RestoreStrategy.OVERWRITE -> deleteAllPageRecords()
        }

        val restorationAction = completableOf {
            pageRecords.forEach(::insert)
        }

        return preRestorationAction
            .andThen(restorationAction)
    }

    private fun deleteAllPageRecords(): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(pageRecordClass)
                    .findAll()
                    ?.deleteAllFromRealm()
            }
        }
    }
}