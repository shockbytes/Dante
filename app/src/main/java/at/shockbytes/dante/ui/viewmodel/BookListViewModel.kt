package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.sort.SortStrategy
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    12.06.2018
 */
class BookListViewModel @Inject constructor(private val bookDao: BookEntityDao,
                                            private val settings: DanteSettings,
                                            private val tracker: Tracker) : BaseViewModel() {

    var state: BookState = BookState.READING

    private val books = MutableLiveData<List<BookEntity>>()

    private var sortComparator: Comparator<BookEntity> = SortComparators.of(settings.sortStrategy)

    init {
        poke()
    }

    override fun poke() {
        bookDao.bookObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { fetchedBooks ->
                    val displayBooks = fetchedBooks
                            .filter { it.state == state }
                            .sortedWith(sortComparator)
                    books.value = displayBooks
                }.addTo(compositeDisposable)

        settings.observeSortStrategy()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    sortComparator = SortComparators.of(it)
                    books.value = books.value?.sortedWith(sortComparator)
                }.addTo(compositeDisposable)
    }

    fun getBooks(): LiveData<List<BookEntity>> = books

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

        tracker.trackEvent(DanteTrackingEvent.BookFinishedEvent(book))
    }

}