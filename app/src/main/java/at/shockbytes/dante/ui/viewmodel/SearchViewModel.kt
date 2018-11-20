package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.view.View
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookSearchItem
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.addTo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val bookDownloader: BookDownloader,
        private val bookDao: BookEntityDao) : BaseViewModel() {

    private val bookTransform: ((BookEntity) -> BookSearchItem) = { b ->
        BookSearchItem(b.id, b.title, b.author, b.thumbnailAddress, b.isbn)
    }

    private val searchState = MutableLiveData<SearchState>()

    private val bookDownloadSubject = PublishSubject.create<BookSearchItem>()

    val bookDownloadEvent: Observable<BookSearchItem> = bookDownloadSubject

    init {
        poke()
    }

    override fun poke() {
        searchState.postValue(SearchState.InitialState)
    }

    fun getSearchState(): LiveData<SearchState> = searchState

    fun showBooks(query: CharSequence, keepLocal: Boolean) {

        if (!query.isEmpty()) {

            searchState.postValue(SearchState.LoadingState)

            resolveSource(query.toString(), keepLocal).subscribe({
                if (it.isNotEmpty()) {
                    searchState.postValue(SearchState.SuccessState(it))
                } else {
                    searchState.postValue(SearchState.EmptyState)
                }
            }, {
                Timber.e(it)
                searchState.postValue(SearchState.ErrorState(it))
            }).addTo(compositeDisposable)
        }
    }

    private fun resolveSource(query: String, keepLocal: Boolean): Flowable<List<BookSearchItem>> {
        return if (keepLocal) localSearch(query) else onlineSearch(query)
    }

    private fun localSearch(query: String): Flowable<List<BookSearchItem>> {
        return bookDao.search(query)
                .map { it.map { b -> bookTransform(b) } }
    }

    private fun onlineSearch(query: String): Flowable<List<BookSearchItem>> {
        return bookDownloader.downloadBook(query)
                .map { b ->
                    val list = mutableListOf<BookSearchItem>()
                    if (b.hasSuggestions) {
                        b.mainSuggestion?.let {
                            list.add(bookTransform(b.mainSuggestion))
                            b.otherSuggestions
                                    .asSequence()
                                    .filter { it.isbn.isNotEmpty() }
                                    .mapTo(list) { book -> bookTransform(book) }
                        }
                    }
                    list.toList()
                }
                .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun requestBookDownload(item: BookSearchItem) {
        bookDownloadSubject.onNext(item)
    }

    fun requestInitialState() {
        poke()
    }

    sealed class SearchState {
        object InitialState: SearchState()
        object LoadingState : SearchState()
        object EmptyState : SearchState()
        class ErrorState(val throwable: Throwable) : SearchState()
        class SuccessState(val items: List<BookSearchItem>) : SearchState()
    }

}