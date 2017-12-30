package at.shockbytes.dante.util.books

import android.support.v4.app.FragmentActivity
import android.util.Log
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.backup.BackupManager
import io.reactivex.Completable
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
                       private val realm: Realm) : BookManager {

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            var config = realm.where(BookConfig::class.java).findFirst()
            if (config == null) {
                config = realm.createObject(BookConfig::class.java)
            }

            return config!!.lastPrimaryKey
        }

    override fun addBook(book: Book): Book {

        realm.beginTransaction()

        val id = lastId
        book.id = id
        realm.copyToRealm(book)

        realm.commitTransaction()

        return book
    }

    override fun getBook(id: Long): Book? {
        return realm.where(Book::class.java).equalTo("id", id).findFirst()
    }

    override fun updateBookState(book: Book, newState: Book.State) {

        realm.executeTransaction { realm ->
            book.state = newState
            realm.copyToRealmOrUpdate(book)
        }

    }

    override fun updateCurrentBookPage(book: Book, page: Int) {

        realm.executeTransaction { realm ->
            book.currentPage = page
            realm.copyToRealmOrUpdate(book)
        }

    }

    override fun updateBookStateAndPage(book: Book, state: Book.State,
                                        page: Int) {

        realm.executeTransaction { realm ->
            book.currentPage = page
            book.state = state
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun removeBook(id: Long) {

        realm.executeTransaction { realm -> realm.where(Book::class.java)
                .equalTo("id", id).findFirst()!!.deleteFromRealm() }
    }

    override fun getStatistics(): Map<String, Int> {

        val stats = HashMap<String, Int>()

        val upcoming = realm.where(Book::class.java)
                .equalTo("ordinalState", Book.State.READ_LATER.ordinal).findAll().size
        val current = realm.where(Book::class.java)
                .equalTo("ordinalState", Book.State.READING.ordinal).findAll().size
        val doneList = realm.where(Book::class.java)
                .equalTo("ordinalState", Book.State.READ.ordinal).findAll()
        val done = doneList.size
        val pages = doneList.sumBy { it.pageCount }
        stats.put(AppParams.STAT_UPCOMING, upcoming)
        stats.put(AppParams.STAT_CURRENT, current)
        stats.put(AppParams.STAT_DONE, done)
        stats.put(AppParams.STAT_PAGES, pages)
        return stats
    }

    override fun downloadBook(isbn: String): Observable<BookSuggestion> {
        return bookDownloader.downloadBookSuggestion(isbn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getAllBooks(): Observable<List<Book>> {
        val books = realm.where(Book::class.java)
                .findAll()
                .sort("id", Sort.DESCENDING)
        return Observable.just<List<Book>>(books)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun restoreBackup(activity: FragmentActivity, backupBooks: List<Book>,
                               strategy: BackupManager.RestoreStrategy): Completable {

        return when (strategy) {

            BackupManager.RestoreStrategy.MERGE -> mergeBackupRestore(activity, backupBooks)
            BackupManager.RestoreStrategy.OVERWRITE -> overwriteBackupRestore(activity, backupBooks)
        }
    }

    override fun getAllBooksSync(): List<Book> {
        return realm.where(Book::class.java)
                .findAll()
                .sort("id", Sort.DESCENDING)
    }

    override fun getBookByState(state: Book.State): Observable<List<Book>> {
        val books = realm.where(Book::class.java)
                .equalTo("ordinalState", state.ordinal)
                .findAll()
                .sort("id", Sort.DESCENDING)
        return Observable.just<List<Book>>(books)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun close() {
        realm.close()
    }

    private fun mergeBackupRestore(activity: FragmentActivity,
                                   backupBooks: List<Book>): Completable {

        return Completable.create {
            // TODO Why not with subscriptions?!?!?!
            activity.runOnUiThread {
                val books = allBooksSync
                for (bBook in backupBooks) {
                    val insert = books.none { it.title == bBook.title }
                    if (insert) {
                        addBook(bBook)
                    }
                }
            }
        }

    }

    private fun overwriteBackupRestore(activity: FragmentActivity,
                                       backupBooks: List<Book>): Completable {

        return Completable.create {
            Log.wtf("Dante", "Overwrite completable")
            // TODO Why not with subscriptions?!?!?!
            activity.runOnUiThread {
                
                Log.wtf("Dante", "Run on ui thread")
                val stored = realm.where(Book::class.java).findAll()
                realm.beginTransaction()
                stored.deleteAllFromRealm()
                realm.commitTransaction()

                for (b in backupBooks) {
                    addBook(b)
                }
            }
        }

    }

}
