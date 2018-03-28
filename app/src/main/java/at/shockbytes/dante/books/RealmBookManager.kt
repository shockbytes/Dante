package at.shockbytes.dante.books

import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.books.BookConfig
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Case
import io.realm.Realm
import io.realm.Sort


/**
 * @author Martin Macheiner
 * Date: 27.08.2016.
 */
class RealmBookManager(private val bookDownloader: BookDownloader,
                       private val realm: Realm) : BookManager {

    private val bookClass = Book::class.java
    private val configClass = BookConfig::class.java

    override val statistics: Single<BookStatistics>
        get() {
            return Single.fromCallable {

                val upcoming = realm.where(bookClass)
                        .equalTo("ordinalState", Book.State.READ_LATER.ordinal).findAll()
                val done = realm.where(bookClass)
                        .equalTo("ordinalState", Book.State.READ.ordinal).findAll()

                val pagesRead = done.sumBy { it.pageCount }
                val pagesWaiting = upcoming.sumBy { it.pageCount }
                val (fastestBook, slowestBook) = BookStatistics.bookDurations(done)
                val avgBooksPerMonth = BookStatistics.averageBooksPerMonth(done)
                val mostReadingMonth = BookStatistics.mostReadingMonth(done)

                BookStatistics(pagesRead, pagesWaiting,
                        done.size, upcoming.size,
                        fastestBook, slowestBook,
                        avgBooksPerMonth, mostReadingMonth)
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
        }

    override val allBooks: Observable<List<Book>>
        get() {
            return Observable.fromCallable {
                val managedModel = realm.where(bookClass)
                        .findAll().sort("id", Sort.DESCENDING).toList()
                realm.copyFromRealm(managedModel)
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
        }

    override val allBooksSync: List<Book>
        get() = realm.where(bookClass).findAll().sort("id", Sort.DESCENDING).toList()

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

    override fun updateBookCover(book: Book, thumbnailAddress: String) {
        realm.executeTransaction {
            book.thumbnailAddress = thumbnailAddress
            realm.copyToRealmOrUpdate(book)
        }
    }

    override fun updateBookPublishedDate(book: Book, publishedDate: String) {
        realm.executeTransaction {
            book.publishedDate = publishedDate
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

    override fun updateBookPositions(books: List<Book>?) {
        realm.executeTransaction {
            books?.let {
                it.forEachIndexed { index, book ->
                    book.position = index
                    realm.copyToRealmOrUpdate(book)
                }
            }
        }
    }

    override fun removeBook(id: Long) {
        realm.executeTransaction {
            realm.where(bookClass)
                    .equalTo("id", id).findFirst()!!.deleteFromRealm()
        }
    }

    override fun downloadBook(isbn: String?): Observable<BookSuggestion> {
        return if (isbn != null) {
            bookDownloader.downloadBookSuggestion(isbn)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        } else {
            Observable.error(NullPointerException("ISBN is null"))
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
                    .sort("position", Sort.ASCENDING).toList()
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun searchBooks(query: String): Flowable<List<Book>> {
        return realm.where(bookClass)
                .contains("title", query, Case.INSENSITIVE)
                .findAll()
                .asFlowable()
                .map { it.toList() }
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
