package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.ui.adapter.pagerecords.PageRecordDetailItem
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import timber.log.Timber
import javax.inject.Inject

class PageRecordsDetailViewModel @Inject constructor(
        private val pageRecordDao: PageRecordDao,
        private val schedulers: SchedulerFacade
): BaseViewModel() {

    private val records = MutableLiveData<List<PageRecordDetailItem>>()
    fun getRecords(): LiveData<List<PageRecordDetailItem>> = records

    fun initialize(bookId: Long) {
        pageRecordDao.pageRecordsForBook(bookId)
                .map(::mapPageRecordToPageRecordDetailItem)
                .subscribeOn(schedulers.io)
                .subscribe(records::postValue, Timber::e)
                .addTo(compositeDisposable)
    }

    private fun mapPageRecordToPageRecordDetailItem(
            pageRecords: List<PageRecord>
    ): List<PageRecordDetailItem> {
        return pageRecords.map { record ->
            // TODO Map properly
            PageRecordDetailItem("Pages", "1234")
        }
    }
}