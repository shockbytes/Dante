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

    fun instance(): Realm {
        val realm = Realm.getDefaultInstance()
        realm.refresh()
        return realm
    }

    inline fun <reified T : RealmObject> createObject(): T {
        return instance().createObject(T::class.java)
    }

    inline fun <reified T : RealmObject> read(): RealmQuery<T> {
        return instance().where(T::class.java)
    }

    fun executeTransaction(block: (Realm) -> Unit) {
        instance().use { realm ->
            realm.executeTransaction(block)
        }
    }
}