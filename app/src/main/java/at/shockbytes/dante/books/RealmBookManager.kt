package at.shockbytes.dante.books

import android.content.Context
import android.content.SharedPreferences
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.books.BookConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.Sort
import java.util.*


/**
 * @author Martin Macheiner
 * Date: 27.08.2016.
 */
class RealmBookManager(private val bookDownloader: BookDownloader,
                       private val realm: Realm,
                       private val context: Context,
                       private val prefs: SharedPreferences) : BookManager {

    private val bookClass = Book::class.java
    private val configClass = BookConfig::class.java

    override var pageTrackingEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_page_tracking_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_page_tracking_key), value)
                    .apply()
        }

    override val statistics: Observable<MutableMap<String, Int>>
        get() {
            return Observable.fromCallable {
                val stats: MutableMap<String, Int> = HashMap()

                val upcoming = realm.where(bookClass)
                        .equalTo("ordinalState", Book.State.READ_LATER.ordinal).findAll().size
                val current = realm.where(bookClass)
                        .equalTo("ordinalState", Book.State.READING.ordinal).findAll().size
                val doneList = realm.where(bookClass)
                        .equalTo("ordinalState", Book.State.READ.ordinal).findAll()
                val done = doneList.size
                val pages = doneList.sumBy { it.pageCount }
                stats.put(AppParams.statKeyUpcoming, upcoming)
                stats.put(AppParams.statKeyCurrent, current)
                stats.put(AppParams.statKeyDone, done)
                stats.put(AppParams.statKeyPages, pages)
                stats
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
        }

    override val allBooks: Observable<List<Book>>
        get() {
            return Observable.fromCallable {
                realm.where(bookClass)
                        .findAll()
                        .sort("id", Sort.DESCENDING).toList()
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
        }

    override val allBooksSync: List<Book>
        get() = realm.where(bookClass).findAll().sort("id", Sort.DESCENDING)

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            val config = realm.where(configClass).findFirst()
                    ?: realm.createObject(configClass)
            return config.getLastPrimaryKey()
        }

    override fun addBook(book: Book): Book {

        realm.beginTransaction()

        val id = lastId
        book.id = id
        realm.copyToRealm(book)

        realm.commitTransaction()

        return book
    }

    override fun getBook(id: Long): Book {
        return realm.where(bookClass).equalTo("id", id).findFirst()!!
    }

    override fun updateBookState(book: Book, newState: Book.State) {
        realm.executeTransaction {
            book.state = newState
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateCurrentBookPage(book: Book, page: Int) {
        realm.executeTransaction {
            book.currentPage = page
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateBookPages(book: Book, currentPage: Int, pageCount: Int) {
        realm.executeTransaction {
            book.currentPage = currentPage
            book.pageCount = pageCount
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateBookNotes(book: Book, notes: String) {
        realm.executeTransaction {
            book.notes = notes
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateBookStateAndPage(book: Book, state: Book.State,
                                        page: Int) {
        realm.executeTransaction {
            book.currentPage = page
            book.state = state
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateBookRating(book: Book, rating: Int) {
        realm.executeTransaction {
            book.rating = rating
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun removeBook(id: Long) {
        realm.executeTransaction {
            realm.where(bookClass)
                    .equalTo("id", id).findFirst()!!.deleteFromRealm()
        }
    }

    override fun downloadBook(isbn: String?): Observable<BookSuggestion> {
        return if (isbn == null) {
            Observable.error(NullPointerException("ISBN is null"))
        } else {
            bookDownloader.downloadBookSuggestion(isbn)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun restoreBackup(backupBooks: List<Book>, strategy: BackupManager.RestoreStrategy) {

        when (strategy) {

            BackupManager.RestoreStrategy.MERGE -> mergeBackupRestore(backupBooks)
            BackupManager.RestoreStrategy.OVERWRITE -> overwriteBackupRestore(backupBooks)
        }
    }

    override fun getBooksByState(state: Book.State): Observable<List<Book>> {
        return Observable.fromCallable {
            realm.where(bookClass)
                    .equalTo("ordinalState", state.ordinal)
                    .findAll()
                    .sort("id", Sort.DESCENDING).toList()
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun close() {
        realm.close()
    }

    private fun mergeBackupRestore(backupBooks: List<Book>) {

        val books = allBooksSync
        for (bBook in backupBooks) {
            val insert = books.none { it.title == bBook.title }
            if (insert) {
                addBook(bBook)
            }
        }
    }

    private fun overwriteBackupRestore(backupBooks: List<Book>) {

        val stored = realm.where(bookClass).findAll()
        realm.beginTransaction()
        stored.deleteAllFromRealm()
        realm.commitTransaction()

        backupBooks.forEach { addBook(it) }
    }

}
