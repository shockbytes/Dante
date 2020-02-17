package at.shockbytes.dante.core.book.realm

import io.realm.RealmObject

open class RealmBookLabel(
    var bookId: Long = -1,
    var title: String = "",
    var hexColor: String = ""
) : RealmObject()