package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.timeline.TimeLineBuilder
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.dante.util.ExceptionHandlers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class TimelineViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    sealed class TimeLineState {

        object Loading : TimeLineState()
        object Error : TimeLineState()
        object Empty : TimeLineState()
        data class Success(val content: List<TimeLineItem>) : TimeLineState()
    }

    private val timeLineState = MutableLiveData<TimeLineState>()
    fun getTimeLineState(): LiveData<TimeLineState> = timeLineState

    fun requestTimeline() {
        bookRepository.bookObservable
            .doOnSubscribe { postState(TimeLineState.Loading) }
            .doOnError { postState(TimeLineState.Error) }
            .map(TimeLineBuilder::buildTimeLineItems)
            .map(::mapItemsToState)
            .subscribe(::postState, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun postState(state: TimeLineState) {
        timeLineState.postValue(state)
    }

    private fun mapItemsToState(items: List<TimeLineItem>): TimeLineState {
        return if (items.isNotEmpty()) {
            TimeLineState.Success(items)
        } else {
            TimeLineState.Empty
        }
    }
}