package at.shockbytes.dante.books

import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.util.books.Book
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 27.08.2016.
 */
interface BookManager {

    val statistics: Observable<MutableMap<String, Int>>

    val allBooks: Observable<List<Book>>

    val allBooksSync: List<Book>

    fun addBook(book: Book): Book

    fun getBook(id: Long): Book

    fun updateBookState(book: Book, newState: Book.State)

    fun updateCurrentBookPage(book: Book, page: Int)

    fun updateBookStateAndPage(book: Book, state: Book.State, page: Int)

    fun updateBookRating(book: Book, rating: Int)

    fun removeBook(id: Long)

    fun downloadBook(isbn: String?): Observable<BookSuggestion>

    fun restoreBackup(backupBooks: List<Book>, strategy: BackupManager.RestoreStrategy)

    fun getBooksByState(state: Book.State): Observable<List<Book>>

    fun close()

}
