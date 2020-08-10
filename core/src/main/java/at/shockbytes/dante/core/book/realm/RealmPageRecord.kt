package at.shockbytes.dante.core.book.realm

import io.realm.RealmObject

open class RealmPageRecord(
        var bookId: Long = -1L,
        var fromPage: Int = 0,
        var toPage: Int = 0,
        var date: String = "" // TODO Find suitable data type
) : RealmObject()