package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.ui.adapter.main.BookAdapterEntity
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.sort.SortStrategy
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val settings: DanteSettings,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    var state: BookState = BookState.READING
        set(value) {
            field = value
            loadBooks()
        }

    sealed class BookLoadingState {

        data class Success(val books: List<BookAdapterEntity>) : BookLoadingState()

        data class Error(val throwable: Throwable) : BookLoadingState()

        object Empty : BookLoadingState()
    }

    private val books = MutableLiveData<BookLoadingState>()
    fun getBooks(): LiveData<BookLoadingState> = books

    private val sortComparator: Comparator<BookEntity>
        get() = SortComparators.of(settings.sortStrategy)

    init {
        listenToSettings()
    }

    private fun loadBooks() {
        bookRepository.bookObservable
            .map { fetchedBooks ->
                fetchedBooks
                    .filter { it.state == state }
                    .sortedWith(sortComparator)
            }
            .map(::mapBooksToBookLoadingState)
            .subscribe(books::postValue) { throwable ->
                Timber.e(throwable, "Cannot fetch books from storage!")
                books.postValue(BookLoadingState.Error(throwable))
            }
            .addTo(compositeDisposable)
    }

    private fun mapBooksToBookLoadingState(books: List<BookEntity>): BookLoadingState {
        return if (books.isNotEmpty()) {

            // TODO Replace true with DanteSettings value
            val bookAdapterEntities = if (state == BookState.READ_LATER) {
                books.toAdapterEntities().apply {
                    toMutableList().add(0, BookAdapterEntity.RandomPick)
                }
            } else {
                books.toAdapterEntities()
            }

            BookLoadingState.Success(bookAdapterEntities)
        } else {
            BookLoadingState.Empty
        }
    }

    private fun List<BookEntity>.toAdapterEntities(): List<BookAdapterEntity> {
        return this.map(BookAdapterEntity::Book)
    }

    private fun listenToSettings() {
        settings.observeSortStrategy()
            .observeOn(schedulers.ui)
            .subscribe {
                loadBooks()
            }
            .addTo(compositeDisposable)
    }

    fun deleteBook(book: BookEntity) {
        bookRepository.delete(book.id)
    }

    fun updateBookPositions(data: MutableList<BookAdapterEntity>) {
        data.forEachIndexed { index, entity ->
            if (entity is BookAdapterEntity.Book) {
                entity.bookEntity.position = index
                bookRepository.update(entity.bookEntity)
            }
        }

        // Update book strategy, because this means user falls back to default strategy
        settings.sortStrategy = SortStrategy.POSITION
    }

    fun moveBookToUpcomingList(book: BookEntity) {
        book.updateState(BookState.READ_LATER)
        bookRepository.update(book)
    }

    fun moveBookToCurrentList(book: BookEntity) {
        book.updateState(BookState.READING)
        bookRepository.update(book)
    }

    fun moveBookToDoneList(book: BookEntity) {
        book.updateState(BookState.READ)
        bookRepository.update(book)
    }

    fun onBookUpdatedEvent(updatedBookState: BookState) {
        if (updatedBookState == state) {
            loadBooks()
        }
    }
}