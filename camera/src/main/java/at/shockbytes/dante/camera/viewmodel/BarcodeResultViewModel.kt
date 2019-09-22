package at.shockbytes.dante.camera.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLoadingState
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.core.network.BookDownloader
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BarcodeResultViewModel(
    private val bookDownloader: BookDownloader,
    private val schedulerFacade: SchedulerFacade,
    private val bookDao: BookEntityDao
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val bookLoadingState = MutableLiveData<BookLoadingState>()
    fun getBookLoadingState(): LiveData<BookLoadingState> = bookLoadingState

    private val bookStoredSubject = PublishSubject.create<String>()
    fun onBookStoredEvent(): Observable<String> = bookStoredSubject

    init {
        bookLoadingState.value = BookLoadingState.Loading
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun loadBook(isbn: String) {
        bookDownloader.downloadBook(isbn)
            .subscribeOn(schedulerFacade.io)
            .subscribe({ suggestion ->
                bookLoadingState.postValue(BookLoadingState.Success(suggestion))
            }, { throwable ->
                Timber.e(throwable)
                bookLoadingState.postValue(BookLoadingState.Error)
            })
            .addTo(compositeDisposable)
    }

    fun storeBook(bookEntity: BookEntity, state: BookState) {
        bookDao.create(
            bookEntity.apply {
                updateState(state)
            }
        )
        bookStoredSubject.onNext(bookEntity.title)
    }
}