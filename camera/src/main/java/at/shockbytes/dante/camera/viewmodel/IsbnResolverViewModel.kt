package at.shockbytes.dante.camera.viewmodel

import androidx.lifecycle.ViewModel
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.core.network.BookDownloader
import io.reactivex.disposables.CompositeDisposable

class IsbnResolverViewModel(
    private val bookDownloader: BookDownloader,
    private val schedulerFacade: SchedulerFacade
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun loadBook(isbn: String) {
        // TODO
    }
}