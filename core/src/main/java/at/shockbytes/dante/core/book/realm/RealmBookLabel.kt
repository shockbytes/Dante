package at.shockbytes.dante.core.book.realm

import io.realm.RealmObject

open class RealmBookLabel(
    var title: String = "",
    var hexColor: String = ""
): RealmObject()