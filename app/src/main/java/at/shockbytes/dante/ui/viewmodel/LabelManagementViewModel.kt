package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor(
    private val bookEntityDao: BookEntityDao,
    private val schedulerFacade: SchedulerFacade
) : BaseViewModel() {

    private val bookLabels = MutableLiveData<List<BookLabel>>()
    fun getBookLabels(): LiveData<List<BookLabel>> = bookLabels

    fun requestAvailableLabels(alreadyAttachedLabels: List<BookLabel>) {

        bookEntityDao.bookLabelObservable
            .flatMapIterable { it }
            .filter { label -> !alreadyAttachedLabels.contains(label) }
            .toList()
            .subscribeOn(schedulerFacade.io)
            .subscribe(bookLabels::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun createNewBookLabel(title: String, hexColor: String) {

        val newLabel = BookLabel(title = title, hexColor = hexColor)
        bookEntityDao.createBookLabel(newLabel)
    }
}
