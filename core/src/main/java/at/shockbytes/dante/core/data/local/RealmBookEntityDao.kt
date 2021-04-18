package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmBook
import at.shockbytes.dante.core.book.realm.RealmBookConfig
import at.shockbytes.dante.core.book.realm.RealmBookLabel
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.merge
import at.shockbytes.dante.util.singleOf
import at.shockbytes.warehouse.util.asObservable
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.realm.Case
import io.realm.Sort

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityDao(private val realm: RealmInstanceProvider) : BookEntityDao {

    private val bookClass = RealmBook::class.java
    private val labelClass = RealmBookLabel::class.java

    private val labelMapper = RealmBookLabelMapper()
    private val mapper: RealmBookEntityMapper = RealmBookEntityMapper(labelMapper)

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            val config = realm.read<RealmBookConfig>(refreshInstance = false).findFirst()
                ?: realm.createObject(refreshInstance = false)
            return config.getLastPrimaryKey()
        }

    override val bookObservable: Observable<List<BookEntity>>
        get() = realm.read<RealmBook>()
            .sort("id", Sort.DESCENDING)
            .findAllAsync()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = realm.read<RealmBookLabel>()
            .equalTo("bookId", BookIds.default())
            .sort("title", Sort.DESCENDING)
            .distinct("title")
            .findAllAsync()
            .asFlowable()
            .map { labelMapper.mapTo(it) }
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)

    override val booksCurrentlyReading: List<BookEntity>
        get() = realm.read<RealmBook>()
            .equalTo("ordinalState", RealmBook.State.READING.ordinal)
            .sort("id", Sort.DESCENDING)
            .findAll()
            .map { mapper.mapTo(it) }

    override operator fun get(id: BookId): Single<BookEntity> {
        return singleOf {
            realm.read<RealmBook>().equalTo("id", id).findFirstAsync()
        }.map(mapper::mapTo)
    }

    override fun create(entity: BookEntity): Completable {
        return completableOf {
            realm.executeTransaction(refreshInstance = false) { realm ->
                val id = lastId
                entity.id = id
                realm.copyToRealm(mapper.mapFrom(entity))
            }
        }
    }

    override fun update(entity: BookEntity): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(mapper.mapFrom(entity))
            }
        }
    }

    override fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
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

    override fun delete(id: BookId): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(bookClass)
                    .equalTo("id", id)
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        return realm.read<RealmBook>()
            .contains("title", query, Case.INSENSITIVE)
            .or()
            .contains("author", query, Case.INSENSITIVE)
            .or()
            .contains("subTitle", query, Case.INSENSITIVE)
            .findAllAsync()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)
    }

    override fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable {
        return when (strategy) {
            RestoreStrategy.MERGE -> mergeBackupRestore(backupBooks)
            RestoreStrategy.OVERWRITE -> overwriteBackupRestore(backupBooks)
        }
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.copyToRealm(labelMapper.mapFrom(bookLabel))
            }
        }
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return Completable.create { emitter ->
            realm.executeTransaction { realm ->
                val labels = realm.where(labelClass)
                    .equalTo("title", bookLabel.title)
                    .and()
                    .equalTo("bookId", bookLabel.bookId)
                    .findAll()

                if (labels != null && labels.isNotEmpty()) {
                    labels.forEach { rbl ->
                        rbl.deleteFromRealm()
                    }
                    emitter.onComplete()
                } else {
                    emitter.tryOnError(RealmBookLabelDeletionException(bookLabel.title))
                }
            }
        }
    }

    private fun mergeBackupRestore(backupBooks: List<BookEntity>): Completable {
        return bookObservable.first(listOf()) // <-- Important! Convert into single first
            .map { books ->
                backupBooks.filter { book ->
                    books.none { it.title == book.title }
                }
            }
            .flatMapCompletable { books ->
                books.map(::create).merge()
            }
    }

    private fun overwriteBackupRestore(backupBooks: List<BookEntity>): Completable {
        val createBackupBooks = backupBooks.map(::create).merge()
        return deleteAllBooks().andThen(createBackupBooks)
    }

    private fun deleteAllBooks(): Completable {
        return completableOf {
            val stored = realm.read<RealmBook>().findAll()
            realm.executeTransaction {
                stored.deleteAllFromRealm()
            }
        }
    }
}