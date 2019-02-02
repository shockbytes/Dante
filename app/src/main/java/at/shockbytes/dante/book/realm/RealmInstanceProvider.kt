package at.shockbytes.dante.book.realm

import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class RealmInstanceProvider(config: RealmConfiguration) {

    init {
        Realm.setDefaultConfiguration(config)
    }

    val instance: Realm
        get() = Realm.getDefaultInstance()
}