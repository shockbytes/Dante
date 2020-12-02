package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class SuggestionsViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository,
    private val bookRepository: BookRepository,
    private val tracker: Tracker
) : BaseViewModel() {

    sealed class SuggestionsState {

        data class Present(val suggestions: List<Suggestion>) : SuggestionsState()

        object Empty : SuggestionsState()
    }

    private val onMoveToWishlistEvent = PublishSubject.create<String>()
    fun onMoveToWishlistEvent(): Observable<String> = onMoveToWishlistEvent

    private val suggestionState = MutableLiveData<SuggestionsState>()
    fun getSuggestionState(): LiveData<SuggestionsState> = suggestionState

    fun requestSuggestions() {
        suggestionsRepository.loadSuggestions()
            .map { suggestions ->
                if (suggestions.suggestions.isEmpty()) {
                    SuggestionsState.Empty
                } else {
                    SuggestionsState.Present(suggestions.suggestions.sortedBy { it.suggestionId })
                }
            }
            .subscribe(suggestionState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun addSuggestionToWishlist(suggestion: Suggestion) {
        bookRepository.create(suggestion.suggestion.toBookEntity())
            .doOnComplete {
                trackAddSuggestionToWishlist(
                    suggestion.suggestionId,
                    suggestion.suggestion.title,
                    suggestion.suggester.name
                )
            }
            .subscribe({
                onMoveToWishlistEvent.onNext(suggestion.suggestion.title)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun BookSuggestionEntity.toBookEntity(): BookEntity {
        return BookEntity(
            title = title,
            subTitle = subTitle,
            author = author,
            state = BookState.WISHLIST,
            pageCount = pageCount,
            publishedDate = publishedDate,
            isbn = isbn,
            thumbnailAddress = thumbnailAddress,
            googleBooksLink = googleBooksLink,
            language = language,
            summary = summary
        )
    }

    private fun trackAddSuggestionToWishlist(
        suggestionId: String,
        bookTitle: String,
        suggester: String
    ) {
        tracker.track(DanteTrackingEvent.AddSuggestionToWishlist(suggestionId, bookTitle, suggester))
    }
}