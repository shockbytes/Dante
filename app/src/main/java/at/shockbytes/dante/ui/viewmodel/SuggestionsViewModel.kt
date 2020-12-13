package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.ui.adapter.suggestions.SuggestionsAdapterItem
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.explanations.Explanations
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
    private val tracker: Tracker,
    private val explanations: Explanations
) : BaseViewModel() {

    sealed class SuggestionsState {

        data class Present(val suggestions: List<SuggestionsAdapterItem>) : SuggestionsState()

        object Error : SuggestionsState()

        object Empty : SuggestionsState()
    }

    sealed class SuggestionEvent {

        data class MoveToWishlistEvent(val title: String) : SuggestionEvent()

        sealed class ReportSuggestionEvent : SuggestionEvent() {

            data class Success(val title: String) : ReportSuggestionEvent()

            data class Error(val title: String) : ReportSuggestionEvent()
        }
    }

    private val onSuggestionEvent = PublishSubject.create<SuggestionEvent>()
    fun onSuggestionEvent(): Observable<SuggestionEvent> = onSuggestionEvent

    private val suggestionState = MutableLiveData<SuggestionsState>()
    fun getSuggestionState(): LiveData<SuggestionsState> = suggestionState

    fun requestSuggestions() {
        suggestionsRepository
            .loadSuggestions(scope = viewModelScope)
            .map { suggestions ->
                if (suggestions.suggestions.isEmpty()) {
                    SuggestionsState.Empty
                } else {
                    SuggestionsState.Present(buildSuggestionsAdapterItems(suggestions))
                }
            }
            .doOnError {
                suggestionState.postValue(SuggestionsState.Error)
            }
            .subscribe(suggestionState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun buildSuggestionsAdapterItems(suggestions: Suggestions): List<SuggestionsAdapterItem> {

        val explanation = explanations.suggestion()

        val suggestedItems = suggestions.suggestions
            .sortedBy { it.suggestionId }
            .map(SuggestionsAdapterItem::SuggestedBook)

        return if (explanation.show) {
            listOf(SuggestionsAdapterItem.Explanation(explanation.userWantsToSuggest)) + suggestedItems
        } else {
            suggestedItems
        }
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
                onSuggestionEvent.onNext(
                    SuggestionEvent.MoveToWishlistEvent(suggestion.suggestion.title)
                )
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

    fun dismissExplanation() {
        explanations.markSeen(explanations.suggestion())
        // Reload after mark explanation as seen
        requestSuggestions()
    }

    fun wantToSuggestBooks() {

        tracker.track(DanteTrackingEvent.InterestedInSuggestingBooks)

        explanations.update(explanations.suggestion().copy(userWantsToSuggest = true))
        // Reload after changing the explanation state
        requestSuggestions()
    }

    fun reportBookSuggestion(suggestionId: String, suggestionTitle: String) {
        suggestionsRepository.reportSuggestion(suggestionId)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                onSuggestionEvent.onNext(
                    SuggestionEvent.ReportSuggestionEvent.Success(suggestionTitle)
                )
            }, {
                onSuggestionEvent.onNext(
                    SuggestionEvent.ReportSuggestionEvent.Error(suggestionTitle)
                )
            })
            .addTo(compositeDisposable)
    }
}