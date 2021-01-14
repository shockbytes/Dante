package at.shockbytes.dante.core.book.realm

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class RealmInstanceProvider(config: RealmConfiguration) {

    init {
        Realm.setDefaultConfiguration(config)
    }

    fun instance(refreshInstance: Boolean = true): Realm {
        val realm = Realm.getDefaultInstance()
        if (refreshInstance) {
            realm.refresh()
        }
        return realm
    }

    inline fun <reified T : RealmObject> createObject(refreshInstance: Boolean = true): T {
        return instance(refreshInstance).createObject(T::class.java)
    }

    inline fun <reified T : RealmObject> read(refreshInstance: Boolean = true): RealmQuery<T> {
        return instance(refreshInstance).where(T::class.java)
    }

    fun executeTransaction(refreshInstance: Boolean = true, block: (Realm) -> Unit) {
        instance(refreshInstance).use { realm ->
            realm.executeTransaction(block)
        }
    }
}