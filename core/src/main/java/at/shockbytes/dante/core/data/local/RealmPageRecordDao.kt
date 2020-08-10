package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.realm.PageRecord
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import io.reactivex.Observable
import io.realm.Sort

class RealmPageRecordDao(private val realm: RealmInstanceProvider) : PageRecordDao {

    private val mapper = RealmPageRecordMapper()

    private val pageRecordClass = RealmPageRecord::class.java

    override fun pageRecordsForBook(bookId: Long): Observable<List<PageRecord>> {
        return realm.instance.where(pageRecordClass)
                .sort("date", Sort.ASCENDING)
                .findAllAsync()
                .asFlowable()
                .map(mapper::mapTo)
                .toObservable()
    }
}