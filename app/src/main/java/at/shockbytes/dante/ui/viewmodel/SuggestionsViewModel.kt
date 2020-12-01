package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import javax.inject.Inject

class SuggestionsViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository,
    private val tracker: Tracker
) : BaseViewModel() {

    sealed class SuggestionsState {

        data class Present(val suggestions: List<Suggestion>) : SuggestionsState()

        object Empty : SuggestionsState()
    }

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

    fun trackAddSuggestionToWishlist(suggestionId: String, bookTitle: String, suggester: String) {
        tracker.track(DanteTrackingEvent.AddSuggestionToWishlist(suggestionId, bookTitle, suggester))
    }
}