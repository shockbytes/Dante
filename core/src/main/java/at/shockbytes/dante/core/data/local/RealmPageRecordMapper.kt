package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.realm.PageRecord
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.Mapper

class RealmPageRecordMapper : Mapper<RealmPageRecord, PageRecord>() {

    override fun mapTo(data: RealmPageRecord): PageRecord {
        return PageRecord(
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                date = data.date
        )
    }

    override fun mapFrom(data: PageRecord): RealmPageRecord {
        return RealmPageRecord(
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                date = data.date
        )
    }
}