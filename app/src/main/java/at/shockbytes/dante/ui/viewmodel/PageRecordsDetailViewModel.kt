package at.shockbytes.dante.ui.viewmodel

import at.shockbytes.dante.core.data.PageRecordDao
import javax.inject.Inject

class PageRecordsDetailViewModel @Inject constructor(
        private val pageRecordDao: PageRecordDao
): BaseViewModel() {

    fun initialize(bookId: Long) {
        // TODO
        pageRecordDao.pageRecordsForBook(bookId)
                .map {

                }
                .subscribe {

                }
    }
}