package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 12-Jun-18.
 */
class BookListViewModel @Inject constructor(private val bookDao: BookEntityDao) : BaseViewModel() {

    var state: BookState = BookState.READING
        set(value) {
            field = value
            filterBooksForState()
        }

    val books = MutableLiveData<List<BookEntity>>()

    private val allBooks = mutableListOf<BookEntity>()

    init {
        poke()
    }

    override fun poke() {
        compositeDisposable.add(bookDao.bookObservable.subscribe { books ->
            allBooks.clear()
            allBooks.addAll(books)
            filterBooksForState()
        })
    }

    private fun filterBooksForState() {
        // TODO Sort books here according to sort strategy
        books.postValue(allBooks
                .filter { it.state == state }
                .sortedBy { it.position })
    }

    fun deleteBook(book: BookEntity) {
        bookDao.delete(book.id)
    }

    fun updateBookPositions(data: MutableList<BookEntity>) {
        data.forEachIndexed { index, book ->
            book.position = index
            bookDao.update(book)
        }
    }

    fun moveBookToUpcomingList(book: BookEntity) {
        book.updateState(BookState.READ_LATER)
        bookDao.update(book)
    }

    fun moveBookToCurrentList(book: BookEntity) {
        book.updateState(BookState.READING)
        bookDao.update(book)
    }

    fun moveBookToDoneList(book: BookEntity) {
        book.updateState(BookState.READ)
        bookDao.update(book)
    }

}