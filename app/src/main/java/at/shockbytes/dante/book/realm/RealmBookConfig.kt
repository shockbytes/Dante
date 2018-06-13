package at.shockbytes.dante.book.realm

import io.realm.RealmObject

/**
 * @author  Martin Macheiner
 * Date:    28.08.2016
 */
open class RealmBookConfig(private var lastPrimaryKey: Long = 0) : RealmObject() {

    fun getLastPrimaryKey(): Long {
        val key = lastPrimaryKey
        lastPrimaryKey++
        return key
    }

}
