package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.book.realm.RealmPageRecord
import at.shockbytes.dante.core.data.Mapper

class RealmPageRecordMapper : Mapper<RealmPageRecord, PageRecord>() {

    override fun mapTo(data: RealmPageRecord): PageRecord {
        return PageRecord(
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                timestamp = data.timestamp
        )
    }

    override fun mapFrom(data: PageRecord): RealmPageRecord {

        val recordId = "${data.bookId}-${data.timestamp}"
        return RealmPageRecord(
                recordId = recordId,
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                timestamp = data.timestamp
        )
    }
}