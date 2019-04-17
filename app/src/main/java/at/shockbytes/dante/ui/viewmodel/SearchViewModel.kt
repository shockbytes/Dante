package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookSearchItem
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.addTo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val bookDownloader: BookDownloader,
    private val bookDao: BookEntityDao
) : BaseViewModel() {

    private val bookTransform: ((BookEntity) -> BookSearchItem) = { b ->
        BookSearchItem(b.id, b.title, b.author, b.thumbnailAddress, b.isbn)
    }

    private val searchState = MutableLiveData<SearchState>()

    private val bookDownloadSubject = PublishSubject.create<BookSearchItem>()

    val bookDownloadEvent: Observable<BookSearchItem> = bookDownloadSubject

    init {
        initialize()
    }

    private fun initialize() {
        searchState.postValue(SearchState.InitialState)
    }

    fun getSearchState(): LiveData<SearchState> = searchState

    fun showBooks(query: CharSequence, keepLocal: Boolean) {

        if (!query.isEmpty()) {

            searchState.postValue(SearchState.LoadingState)

            resolveSource(query.toString(), keepLocal)
                    .subscribe({
                        if (it.isNotEmpty()) {
                            searchState.postValue(SearchState.SuccessState(it))
                        } else {
                            searchState.postValue(SearchState.EmptyState)
                        }
                    }, {
                        Timber.e(it)
                        searchState.postValue(SearchState.ErrorState(it))
                    })
                    .addTo(compositeDisposable)
        }
    }

    private fun resolveSource(query: String, keepLocal: Boolean): Observable<List<BookSearchItem>> {
        return if (keepLocal) localSearch(query) else onlineSearch(query)
    }

    private fun localSearch(query: String): Observable<List<BookSearchItem>> {
        return bookDao.search(query)
                .map { it.map { b -> bookTransform(b) } }
    }

    private fun onlineSearch(query: String): Observable<List<BookSearchItem>> {
        return bookDownloader.downloadBook(query)
                .map { b ->
                    val list = mutableListOf<BookSearchItem>()
                    if (b.hasSuggestions) {
                        b.mainSuggestion?.let { mainSuggestion ->
                            list.add(bookTransform(mainSuggestion))
                            b.otherSuggestions
                                    .asSequence()
                                    .filter { it.isbn.isNotEmpty() }
                                    .mapTo(list) { book -> bookTransform(book) }
                        }
                    }
                    list.toList()
                }
    }

    fun requestBookDownload(item: BookSearchItem) {
        bookDownloadSubject.onNext(item)
    }

    fun requestInitialState() {
        initialize()
    }

    sealed class SearchState {
        object InitialState : SearchState()
        object LoadingState : SearchState()
        object EmptyState : SearchState()
        class ErrorState(val throwable: Throwable) : SearchState()
        class SuccessState(val items: List<BookSearchItem>) : SearchState()
    }
}