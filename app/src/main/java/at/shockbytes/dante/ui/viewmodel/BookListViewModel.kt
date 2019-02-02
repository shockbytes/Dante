package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.sort.SortStrategy
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookListViewModel @Inject constructor(
    private val bookDao: BookEntityDao,
    private val settings: DanteSettings,
    private val tracker: Tracker,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    var state: BookState = BookState.READING
        set(value) {
            field = value
            loadBooks()
        }

    sealed class BookLoadingState {

        data class Success(val books: List<BookEntity>) : BookLoadingState()

        data class Error(val throwable: Throwable) : BookLoadingState()

        object Empty : BookLoadingState()
    }

    private val books = MutableLiveData<BookLoadingState>()

    private var sortComparator: Comparator<BookEntity> = SortComparators.of(settings.sortStrategy)

    init {
        listenToSettings()
    }

    override fun poke() = Unit

    private fun loadBooks() {
        bookDao.bookObservable
                .map { fetchedBooks ->
                    fetchedBooks
                            .filter { it.state == state }
                            .sortedWith(sortComparator)
                }
                .subscribe({ displayBooks ->
                    if (displayBooks.isNotEmpty()) {
                        books.postValue(BookLoadingState.Success(displayBooks))
                    } else {
                        books.postValue(BookLoadingState.Empty)
                    }
                }, { throwable ->
                    Timber.e(throwable, "Cannot fecth books from storage!")
                    books.postValue(BookLoadingState.Error(throwable))
                })
                .addTo(compositeDisposable)
    }

    private fun listenToSettings() {
        settings.observeSortStrategy()
                .observeOn(schedulers.ui)
                .subscribe {
                    sortComparator = SortComparators.of(it)
                    updateIfBooksLoaded()
                }.addTo(compositeDisposable)
    }

    private fun updateIfBooksLoaded() {
        val state = books.value
        if (state is BookLoadingState.Success) {
            books.postValue(state.copy(books = state.books.sortedWith(sortComparator)))
        }
    }

    fun getBooks(): LiveData<BookLoadingState> = books

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