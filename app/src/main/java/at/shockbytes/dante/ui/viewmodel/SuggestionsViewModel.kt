package at.shockbytes.dante.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.shockbytes.dante.R
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
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class SuggestionsViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository,
    private val bookRepository: BookRepository,
    private val tracker: Tracker,
    private val explanations: Explanations,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    sealed class SuggestionsState {

        object Loading : SuggestionsState()

        data class Present(val suggestions: List<SuggestionsAdapterItem>) : SuggestionsState()

        object Error : SuggestionsState()

        object UnauthenticatedUser : SuggestionsState()

        object Empty : SuggestionsState()
    }

    sealed class SuggestionEvent {

        data class MoveToWishlistEvent(val title: String) : SuggestionEvent()

        sealed class ReportSuggestionEvent : SuggestionEvent() {

            data class Success(val title: String) : ReportSuggestionEvent()

            data class Error(val title: String) : ReportSuggestionEvent()
        }

        sealed class LikeSuggestionEvent : SuggestionEvent() {

            data class Success(
                @StringRes val messageStringRes: Int,
                val title: String
            ) : LikeSuggestionEvent()

            data class Error(val title: String) : LikeSuggestionEvent()
        }
    }

    private val onSuggestionEvent = PublishSubject.create<SuggestionEvent>()
    fun onSuggestionEvent(): Observable<SuggestionEvent> = onSuggestionEvent

    private val suggestionState = MutableLiveData<SuggestionsState>()
    fun getSuggestionState(): LiveData<SuggestionsState> = suggestionState

    fun requestSuggestions() {

        Single
            .zip(
                bookRepository.bookObservable.firstOrError(),
                suggestionsRepository.loadSuggestions(scope = viewModelScope),
                { books, suggestions -> Pair(books, suggestions) }
            )
            .doOnSubscribe {
                suggestionState.postValue(SuggestionsState.Loading)
            }
            .map { (books, reports) ->
                buildSuggestionsState(books, reports)
            }
            .doOnError { throwable ->
                val errorState = if (throwable.isUnauthenticatedException()) {
                    SuggestionsState.UnauthenticatedUser
                } else {
                    SuggestionsState.Error
                }
                suggestionState.postValue(errorState)
            }
            .subscribe(suggestionState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun Throwable.isUnauthenticatedException(): Boolean {
        return (this is HttpException) && (this.code() == 403)
    }

    private fun buildSuggestionsState(
        books: List<BookEntity>,
        suggestions: Suggestions
    ): SuggestionsState {
        val suggestedItems = suggestions.suggestions
            .sortedBy { it.suggestionId }
            .filter { suggestion ->
                // Check if book isn't already added in the library
                // and if hasn't been reported by this user
                val bookAlreadyAdded = books.find {
                    it.title == suggestion.suggestion.title
                } != null
                !bookAlreadyAdded && !suggestion.isReportedByMe
            }
            .map(SuggestionsAdapterItem::SuggestedBook)

        return when {
            suggestedItems.isEmpty() -> SuggestionsState.Empty
            explanations.suggestion().show -> {
                val items = listOf(SuggestionsAdapterItem.SuggestionHint()) + suggestedItems
                SuggestionsState.Present(items)
            }
            else -> SuggestionsState.Present(suggestedItems)
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
        tracker.track(
            DanteTrackingEvent.AddSuggestionToWishlist(
                suggestionId,
                bookTitle,
                suggester
            )
        )
    }

    fun dismissExplanation() {
        explanations.markSeen(explanations.suggestion())
        // Reload after mark explanation as seen
        requestSuggestions()
    }

    fun reportBookSuggestion(suggestionId: String, suggestionTitle: String) {
        suggestionsRepository.reportSuggestion(suggestionId, scope = viewModelScope)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .observeOn(schedulers.ui)
            .doOnComplete {
                // Reload after a book has been liked
                requestSuggestions()
            }
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

    fun likeSuggestion(suggestionId: String, suggestionTitle: String, isLikedByMe: Boolean) {
        suggestionsRepository.likeSuggestion(suggestionId, isLikedByMe, scope = viewModelScope)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .observeOn(schedulers.ui)
            .doOnComplete {
                // Reload after a book has been liked
                requestSuggestions()
            }
            .subscribe({

                val msgRes = if (isLikedByMe) {
                    R.string.suggestion_dislike_template
                } else R.string.suggestion_like_template

                onSuggestionEvent.onNext(
                    SuggestionEvent.LikeSuggestionEvent.Success(msgRes, suggestionTitle)
                )
            }, {
                onSuggestionEvent.onNext(
                    SuggestionEvent.LikeSuggestionEvent.Error(suggestionTitle)
                )
            })
            .addTo(compositeDisposable)
    }
}