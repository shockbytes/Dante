package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmBook
import at.shockbytes.dante.core.book.realm.RealmBookConfig
import at.shockbytes.dante.core.book.realm.RealmBookLabel
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.maybeOf
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.realm.Case
import io.realm.Sort
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityDao(private val realm: RealmInstanceProvider) : BookEntityDao {

    private val bookClass = RealmBook::class.java
    private val configClass = RealmBookConfig::class.java
    private val labelClass = RealmBookLabel::class.java

    private val labelMapper = RealmBookLabelMapper()
    private val mapper: RealmBookEntityMapper = RealmBookEntityMapper(labelMapper)

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            val config = realm.instance.where(configClass).findFirst()
                ?: realm.instance.createObject(configClass)
            return config.getLastPrimaryKey()
        }

    override val bookObservable: Observable<List<BookEntity>>
        get() = realm.instance.where(bookClass)
            .sort("id", Sort.DESCENDING)
            .findAllAsync()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = realm.instance.where(labelClass)
            .equalTo("bookId", BookLabel.UNASSIGNED_LABEL_ID)
            .sort("title", Sort.DESCENDING)
            .distinct("title")
            .findAllAsync()
            .asFlowable()
            .map { labelMapper.mapTo(it) }
            .toObservable()

    override val booksCurrentlyReading: List<BookEntity>
        get() = realm.instance.where(bookClass)
            .equalTo("ordinalState", RealmBook.State.READING.ordinal)
            .sort("id", Sort.DESCENDING)
            .findAll()
            .map { mapper.mapTo(it) }

    override operator fun get(id: Long): Maybe<BookEntity> {
        return maybeOf {
            realm.instance.where(bookClass).equalTo("id", id).findFirst()
        }.map(mapper::mapTo)
    }

    override fun create(entity: BookEntity): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                val id = lastId
                entity.id = id
                realm.copyToRealm(mapper.mapFrom(entity))
            }
        }
    }

    override fun update(entity: BookEntity): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(mapper.mapFrom(entity))
            }
        }
    }

    override fun updateCurrentPage(bookId: Long, currentPage: Int): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                realm.where(bookClass)
                    .equalTo("id", bookId)
                    .findFirst()
                    ?.let { realmBook ->
                        realmBook.currentPage = currentPage
                        realm.copyToRealmOrUpdate(realmBook)
                    }
            }
        }
    }

    override fun delete(id: Long): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                realm.where(bookClass)
                    .equalTo("id", id)
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        return realm.instance.where(bookClass)
            .contains("title", query, Case.INSENSITIVE)
            .or()
            .contains("author", query, Case.INSENSITIVE)
            .or()
            .contains("subTitle", query, Case.INSENSITIVE)
            .findAll()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()
    }

    override fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable {
        return Completable.fromAction {
            when (strategy) {
                RestoreStrategy.MERGE -> mergeBackupRestore(backupBooks)
                RestoreStrategy.OVERWRITE -> overwriteBackupRestore(backupBooks)
            }
        }
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                realm.copyToRealm(labelMapper.mapFrom(bookLabel))
            }
        }
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return completableOf {
            realm.instance.executeTransaction { realm ->
                realm.where(labelClass)
                    .equalTo("title", bookLabel.title)
                    .and()
                    .equalTo("bookId", bookLabel.bookId)
                    .findFirst()
                    ?.deleteFromRealm()
                    ?: Timber.e(RealmBookLabelDeletionException(bookLabel.title))
            }
        }
    }

    private fun getBooks(): List<RealmBook> {
        return realm.instance.where(bookClass).findAll().toList()
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

        val stored = realm.instance.where(bookClass).findAll()
        realm.instance.executeTransaction {
            stored.deleteAllFromRealm()
        }

        backupBooks.forEach { book ->
            create(book)
        }
    }
}