package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.sort.SortStrategy
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    12.06.2018
 */
class BookListViewModel @Inject constructor(private val bookDao: BookEntityDao,
                                            private val settings: DanteSettings) : BaseViewModel() {

    var state: BookState = BookState.READING
        set(value) {
            field = value
            updateBooks()
        }

    val books = MutableLiveData<List<BookEntity>>()

    private val allBooks = mutableListOf<BookEntity>()

    private var sortComparator: Comparator<BookEntity> = SortComparators.of(settings.sortStrategy)

    init {
        poke()
    }

    override fun poke() {
        compositeDisposable.add(bookDao.bookObservable.subscribe { books ->
            allBooks.clear()
            allBooks.addAll(books)
            updateBooks()
        })
        compositeDisposable.add(settings.observeSortStrategy().subscribe {
            sortComparator = SortComparators.of(settings.sortStrategy)
            updateBooks()
        })
    }

    private fun updateBooks() {
        books.postValue(allBooks
                .asSequence()
                .filter { it.state == state }
                .sortedWith(sortComparator)
                .toList())
    }

    fun deleteBook(book: BookEntity) {
        bookDao.delete(book.id)
    }

    fun updateBookPositions(data: MutableList<BookEntity>) {
        data.forEachIndexed { index, book ->
            book.position = index
            bookDao.update(book)
        }

        // Update book strategy, because this means user falls back to default strategy
        settings.sortStrategy = SortStrategy.POSITION
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