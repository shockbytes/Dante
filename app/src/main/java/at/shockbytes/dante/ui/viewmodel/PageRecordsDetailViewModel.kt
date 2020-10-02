package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.ui.adapter.pagerecords.PageRecordDetailItem
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject

class PageRecordsDetailViewModel @Inject constructor(
        private val pageRecordDao: PageRecordDao,
        private val schedulers: SchedulerFacade
): BaseViewModel() {

    private val dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy")

    private val records = MutableLiveData<List<PageRecordDetailItem>>()
    fun getRecords(): LiveData<List<PageRecordDetailItem>> = records

    private var bookId: Long = -1L

    fun initialize(bookId: Long) {
        this.bookId = bookId
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

            val formattedPagesRead = "${record.fromPage} - ${record.toPage}"
            val formattedDate = dateFormat.print(record.timestamp)

            PageRecordDetailItem(record,formattedPagesRead, formattedDate)
        }
    }

    fun deletePageRecord(pageRecord: PageRecord) {

        // TODO Stitch next record properly...
        // TODO Set current page accordingly, if last entry was deleted

        pageRecordDao.deletePageRecordForBook(pageRecord)
                .subscribeOn(schedulers.io)
                .subscribe({
                    initialize(bookId)
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)
    }
}