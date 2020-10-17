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
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val settings: DanteSettings,
    private val schedulers: SchedulerFacade,
    private val danteSettings: DanteSettings,
    private val tracker: Tracker
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

    sealed class RandomPickEvent {

        data class RandomPick(val book: BookEntity) : RandomPickEvent()

        object NoBookAvailable : RandomPickEvent()
    }

    private val pickRandomBookSubject = PublishSubject.create<RandomPickEvent>()
    val onPickRandomBookEvent: Observable<RandomPickEvent> = pickRandomBookSubject
            .delay(300L, TimeUnit.MILLISECONDS) // Delay it a bit, otherwise the UI appears too fast

    init {
        listenToSettings()
    }

    private fun listenToSettings() {
        // Reload books whenever the sort strategy changes
        settings.observeSortStrategy()
                .observeOn(schedulers.ui)
                .subscribe { loadBooks() }
                .addTo(compositeDisposable)

        // Reload books whenever the random pick interaction setting changes
        // but only if we are in the correct tab
        settings.observeRandomPickInteraction()
                .subscribe {
                    if (state == BookState.READ_LATER) {
                        loadBooks()
                    }
                }
                .addTo(compositeDisposable)
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

            val bookAdapterEntities = if (shouldShowRandomPickInteraction(books.size)) {
                books.toAdapterEntities().toMutableList().apply {
                    add(0, BookAdapterEntity.RandomPick)
                }
            } else {
                books.toAdapterEntities()
            }

            BookLoadingState.Success(bookAdapterEntities)
        } else {
            BookLoadingState.Empty
        }
    }

    /**
     * Show random pick interaction if:
     * - User is in read_later tab
     * - There is more than 1 book in this tab
     * - And the user did not explicitly opt-out for it in the settings
     */
    private fun shouldShowRandomPickInteraction(size: Int): Boolean {
        return state == BookState.READ_LATER && size > 1 && danteSettings.showRandomPickInteraction
    }

    private fun List<BookEntity>.toAdapterEntities(): List<BookAdapterEntity> {
        return this.map { entity ->
            BookAdapterEntity.Book(entity)
        }
    }

    fun deleteBook(book: BookEntity) {
        bookRepository.delete(book.id)
            .subscribe({
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun updateBookPositions(data: MutableList<BookAdapterEntity>) {
        data.forEachIndexed { index, entity ->
            if (entity is BookAdapterEntity.Book) {
                entity.bookEntity.position = index
                updateBook(entity.bookEntity)
            }
        }

        // Update book strategy, because this means user falls back to default strategy
        settings.sortStrategy = SortStrategy.POSITION
    }

    fun moveBookToUpcomingList(book: BookEntity) {
        book.updateState(BookState.READ_LATER)
        updateBook(book)
    }

    fun moveBookToCurrentList(book: BookEntity) {
        book.updateState(BookState.READING)
        updateBook(book)
    }

    fun moveBookToDoneList(book: BookEntity) {
        book.updateState(BookState.READ)
        updateBook(book)
    }

    fun updateBook(book: BookEntity) {
        bookRepository.update(book)
            .subscribe({
                Timber.d("Successfully updated ${book.title}")
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun onBookUpdatedEvent(updatedBookState: BookState) {
        if (updatedBookState == state) {
            loadBooks()
        }
    }

    fun pickRandomBookToRead() {

        val event = books.value
                ?.let { state ->
                    (state as? BookLoadingState.Success)?.books
                }
                ?.also { adapterEntities ->
                    val books = adapterEntities.filterIsInstance<BookAdapterEntity.Book>().count()
                    tracker.track(DanteTrackingEvent.PickRandomBook(books))
                }
                ?.let { books ->
                    val randomPick = books
                            .filterIsInstance<BookAdapterEntity.Book>()
                            .random()
                            .bookEntity
                    RandomPickEvent.RandomPick(randomPick)
                }
                ?: RandomPickEvent.NoBookAvailable

        pickRandomBookSubject.onNext(event)
    }

    fun onDismissRandomBookPicker() {
        danteSettings.showRandomPickInteraction = false

        tracker.track(DanteTrackingEvent.DisableRandomBookInteraction)
    }
}