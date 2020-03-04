package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.BookStatsBuilder
import at.shockbytes.dante.util.ExceptionHandlers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val statisticsItems = MutableLiveData<List<BookStatsViewItem>>()
    fun getStatistics(): LiveData<List<BookStatsViewItem>> = statisticsItems

    fun requestStatistics() {
        bookRepository.bookObservable
            .map(BookStatsBuilder::createFrom)
            .subscribe(statisticsItems::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }
}