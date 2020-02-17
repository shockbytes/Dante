package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor(
    private val bookEntityDao: BookEntityDao
) : BaseViewModel() {

    sealed class LabelState {

        object Empty : LabelState()

        data class Present(val labels: List<BookLabel>) : LabelState()
    }

    private val bookLabelState = MutableLiveData<LabelState>()
    fun getBookLabelState(): LiveData<LabelState> = bookLabelState

    private val newLabelRequestSubject = PublishSubject.create<List<BookLabel>>()
    val onCreateNewLabelRequest: Observable<List<BookLabel>> = newLabelRequestSubject

    fun requestAvailableLabels(alreadyAttachedLabels: List<BookLabel>) {
        bookEntityDao.bookLabelObservable
            .map { labels ->
                val filtered = labels.filter { label ->
                    alreadyAttachedLabels.none { it.hexColor == label.hexColor && it.title == label.title }
                }

                if (filtered.isNotEmpty()) {
                    LabelState.Present(filtered)
                } else {
                    LabelState.Empty
                }
            }
            .subscribe(bookLabelState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun createNewBookLabel(newLabel: BookLabel) {
        bookEntityDao.createBookLabel(newLabel)
    }

    fun deleteBookLabel(bookLabel: BookLabel) {
        bookEntityDao.deleteBookLabel(bookLabel)
    }

    fun requestCreateNewLabel() {
        bookLabelState.value?.let { state ->

            val labels = when (state) {
                LabelState.Empty -> listOf()
                is LabelState.Present -> state.labels
            }
            newLabelRequestSubject.onNext(labels)
        }
    }
}
