package at.shockbytes.dante.core.book.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPageRecord(
    @PrimaryKey var recordId: String = "", // of type "bookId-timestamp"
    var bookId: Long = -1L,
    var fromPage: Int = 0,
    var toPage: Int = 0,
    var timestamp: Long = 0L
) : RealmObject()