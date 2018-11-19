package at.shockbytes.dante.data

import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.realm.RealmBook
import at.shockbytes.dante.book.realm.RealmBookConfig
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Case
import io.realm.Realm
import io.realm.Sort

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityDao(private val realm: Realm) : BookEntityDao {

    private val bookClass = RealmBook::class.java
    private val configClass = RealmBookConfig::class.java

    private val mapper: RealmBookEntityMapper = RealmBookEntityMapper()

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            val config = realm.where(configClass).findFirst()
                    ?: realm.createObject(configClass)
            return config.getLastPrimaryKey()
        }

    override val bookObservable: Flowable<List<BookEntity>>
        get() = realm.where(bookClass)
                .findAllAsync().sort("id", Sort.DESCENDING)
                .asFlowable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { mapper.mapTo(it) }

    override fun get(id: Long): BookEntity? {
        val book = realm.where(bookClass).equalTo("id", id).findFirst()
        return if (book != null) {
            mapper.mapTo(book)
        } else null
    }

    override fun create(entity: BookEntity) {

        realm.beginTransaction()

        val id = lastId
        entity.id = id
        realm.copyToRealm(mapper.mapFrom(entity))

        realm.commitTransaction()
    }

    override fun update(entity: BookEntity) {
        realm.executeTransaction {
            realm.copyToRealmOrUpdate(mapper.mapFrom(entity))
        }
    }

    override fun delete(id: Long) {
        realm.executeTransaction {
            realm.where(bookClass)
                    .equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    override fun search(query: String): Flowable<List<BookEntity>> {
        return realm.where(bookClass)
                .contains("title", query, Case.INSENSITIVE)
                .findAll()
                .asFlowable()
                .map { mapper.mapTo(it) }
    }

    override fun restoreBackup(backupBooks: List<BookEntity>, strategy: BackupManager.RestoreStrategy) {

        when (strategy) {
            BackupManager.RestoreStrategy.MERGE -> mergeBackupRestore(backupBooks)
            BackupManager.RestoreStrategy.OVERWRITE -> overwriteBackupRestore(backupBooks)
        }
    }

    private fun getBooks(): List<RealmBook> {
        return realm.where(bookClass).findAll().toList()
    }

    private fun mergeBackupRestore(backupBooks: List<BookEntity>) {

        val books = getBooks()
        for (bBook in backupBooks) {
            val insert = books.none { it.title == bBook.title }
            if (insert) {
                create(bBook)
            }
        }
    }

    private fun overwriteBackupRestore(backupBooks: List<BookEntity>) {

        val stored = realm.where(bookClass).findAll()
        realm.beginTransaction()
        stored.deleteAllFromRealm()
        realm.commitTransaction()

        backupBooks.forEach { create(it) }
    }

}