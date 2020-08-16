package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.BookStatsBuilder
import at.shockbytes.dante.util.ExceptionHandlers
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
        private val bookRepository: BookRepository,
        private val recordDao: PageRecordDao,
        featureFlagging: FeatureFlagging
) : BaseViewModel() {

    private val bookStatsBuilder = BookStatsBuilder(featureFlagging)

    private val statisticsItems = MutableLiveData<List<BookStatsViewItem>>()
    fun getStatistics(): LiveData<List<BookStatsViewItem>> = statisticsItems

    fun requestStatistics() {
        Observable
                .zip(
                        bookRepository.bookObservable,
                        recordDao.allPageRecords(),
                        BiFunction { books: List<BookEntity>, records: List<PageRecord> ->
                            books to records
                        }
                )
                .map { (books, pageRecords) ->
                    bookStatsBuilder.createFrom(books, pageRecords)
                }
                .subscribe(statisticsItems::postValue, ExceptionHandlers::defaultExceptionHandler)
                .addTo(compositeDisposable)
    }
}