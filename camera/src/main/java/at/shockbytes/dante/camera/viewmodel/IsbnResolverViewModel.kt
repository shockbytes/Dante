package at.shockbytes.dante.camera.viewmodel

import androidx.lifecycle.ViewModel
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.core.network.BookDownloader
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class IsbnResolverViewModel(
    private val bookDownloader: BookDownloader,
    private val schedulerFacade: SchedulerFacade,
    private val bookDao: BookEntityDao
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun loadBook(isbn: String) {
        bookDownloader.downloadBook(isbn)
            .subscribeOn(schedulerFacade.io)
            .subscribe({ bookSuggestion ->
                Timber.d(bookSuggestion.toString())
            }, { throwable ->
            })
            .addTo(compositeDisposable)
    }

    fun storeBook(book: BookEntity) {
        // TODO
    }
}