package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookStatistics
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(private val bookDao: BookEntityDao) : BaseViewModel() {

    val statistics = MutableLiveData<BookStatistics>()

    override fun poke() {
        // Load statistics
        requestStatistics()
    }

    fun requestStatistics() {
        bookDao.bookObservable.subscribe { books ->
            BookStatistics.from(books)
                    .subscribeOn(Schedulers.computation())
                    .subscribe { stats ->
                        statistics.postValue(stats)
                    }.addTo(compositeDisposable)
        }.addTo(compositeDisposable)
    }
}