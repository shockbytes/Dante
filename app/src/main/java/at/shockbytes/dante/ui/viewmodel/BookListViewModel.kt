package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.book.Languages
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.login.UserState
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.ui.adapter.main.BookAdapterItem
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.explanations.Explanations
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.sort.SortStrategy
import at.shockbytes.dante.util.toAdapterItems
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
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
    private val tracker: Tracker,
    private val explanations: Explanations,
    private val suggestionsRepository: SuggestionsRepository,
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    var state: BookState = BookState.READING
        set(value) {
            field = value
            loadBooks()
        }

    sealed class BookLoadingState {

        data class Success(val books: List<BookAdapterItem>) : BookLoadingState()

        data class Error(val throwable: Throwable) : BookLoadingState()

        object Empty : BookLoadingState()
    }

    sealed class SuggestionState {

        data class Suggest(val book: BookEntity) : SuggestionState()

        data class WrongLanguage(val currentLanguage: String?) : SuggestionState()

        object UserNotLoggedIn : SuggestionState()
    }

    sealed class Event {

        data class SuggestionPlaced(val textRes: Int) : Event()
    }

    private val eventSubject = PublishSubject.create<Event>()
    fun onEvent(): Observable<Event> = eventSubject

    private val suggestionSubject = PublishSubject.create<SuggestionState>()
    fun onSuggestionEvent(): Observable<SuggestionState> = suggestionSubject

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
            .distinctUntilChanged()
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
            val bookAdapterItems = lookupForHeaderItem(books) + books.toAdapterItems()
            BookLoadingState.Success(bookAdapterItems)
        } else {
            BookLoadingState.Empty
        }
    }

    private fun lookupForHeaderItem(books: List<BookEntity>): List<BookAdapterItem> {
        return when {
            (state == BookState.READ_LATER && shouldShowRandomPickInteraction(books.size)) -> {
                listOf(BookAdapterItem.RandomPick)
            }
            (state == BookState.WISHLIST && explanations.wishlist().show) -> {
                listOf(BookAdapterItem.WishlistExplanation)
            }
            else -> listOf()
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

    fun deleteBook(book: BookEntity) {
        bookRepository.delete(book.id)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun suggestBook(book: BookEntity, recommendation: String) {
        suggestionsRepository.suggestBook(book, recommendation)
            .subscribe({
                eventSubject.onNext(Event.SuggestionPlaced(R.string.suggestion_placed_success))
            }, { throwable ->
                Timber.e(throwable)
                eventSubject.onNext(Event.SuggestionPlaced(R.string.suggestion_placed_error))
            })
            .addTo(compositeDisposable)
    }

    fun updateBookPositions(data: MutableList<BookAdapterItem>) {
        data.forEachIndexed { index, entity ->
            if (entity is BookAdapterItem.Book) {
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

    private fun updateBook(book: BookEntity) {
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
                val books = adapterEntities.filterIsInstance<BookAdapterItem.Book>().count()
                tracker.track(DanteTrackingEvent.PickRandomBook(books))
            }
            ?.let { books ->
                val randomPick = books
                    .filterIsInstance<BookAdapterItem.Book>()
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

    fun dismissWishlistExplanation() {
        explanations.markSeen(explanations.wishlist())
    }

    fun verifyBookSuggestion(book: BookEntity) {
        loginRepository.getAccount()
            .map { state ->
                when {
                    state is UserState.Unauthenticated -> SuggestionState.UserNotLoggedIn
                    Languages.ENGLISH.code != book.language -> SuggestionState.WrongLanguage(book.language)
                    else -> SuggestionState.Suggest(book)
                }
            }
            .observeOn(schedulers.ui)
            .subscribe(suggestionSubject::onNext, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }
}