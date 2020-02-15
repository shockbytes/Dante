package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor(
    private val bookEntityDao: BookEntityDao
) : BaseViewModel() {

    private val bookLabels = MutableLiveData<List<BookLabel>>()
    fun getBookLabels(): LiveData<List<BookLabel>> = bookLabels

    private val newLabelRequestSubject = PublishSubject.create<List<BookLabel>>()
    val onCreateNewLabelRequest: Observable<List<BookLabel>> = newLabelRequestSubject

    fun requestAvailableLabels(alreadyAttachedLabels: List<BookLabel>) {
        bookEntityDao.bookLabelObservable
            .map { labels ->
                labels.filter { label ->
                    alreadyAttachedLabels.none { it.hexColor == label.hexColor && it.title == label.title }
                }
            }
            .subscribe(bookLabels::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun createNewBookLabel(newLabel: BookLabel) {
        bookEntityDao.createBookLabel(newLabel)
    }

    fun deleteBookLabel(bookLabel: BookLabel) {
        bookEntityDao.deleteBookLabel(bookLabel)
    }

    fun requestCreateNewLabel() {
        bookLabels.value?.let(newLabelRequestSubject::onNext)
    }
}
