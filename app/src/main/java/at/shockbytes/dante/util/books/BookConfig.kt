package at.shockbytes.dante.util.books

import io.realm.RealmObject

/**
 * @author Martin Macheiner
 * Date: 28.08.2016.
 */
open class BookConfig(private var lastPrimaryKey: Long = 0) : RealmObject() {

    fun getLastPrimaryKey(): Long {
        val key = lastPrimaryKey
        lastPrimaryKey++
        return key
    }

}
