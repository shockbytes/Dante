package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import timber.log.Timber
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor(
    private val bookEntityDao: BookEntityDao
) : BaseViewModel() {

    private val bookLabels = MutableLiveData<List<BookLabel>>()
    fun getBookLabels(): LiveData<List<BookLabel>> = bookLabels

    fun requestAvailableLabels(alreadyAttachedLabels: List<BookLabel>) {
        bookEntityDao.bookLabelObservable
            .map { labels ->
                labels.filter { label ->
                    !alreadyAttachedLabels.contains(label)
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
}
