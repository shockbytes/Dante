package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.util.addTo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val bookDownloader: BookDownloader,
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val bookTransform: ((BookEntity) -> BookSearchItem) = { b ->
        BookSearchItem(b.id, b.title, b.author, b.thumbnailAddress, b.isbn)
    }

    private val searchState = MutableLiveData<SearchState>()
    fun getSearchState(): LiveData<SearchState> = searchState

    private val bookDownloadSubject = PublishSubject.create<BookSearchItem>()

    val bookDownloadEvent: Observable<BookSearchItem> = bookDownloadSubject

    init {
        initialize()
    }

    private fun initialize() {
        searchState.postValue(SearchState.InitialState)
    }

    fun showBooks(query: CharSequence, keepLocal: Boolean) {

        if (query.isNotEmpty()) {

            resolveSource(query.toString(), keepLocal)
                    .doOnSubscribe {
                        searchState.postValue(SearchState.LoadingState)
                    }
                    .map { bookSearchItems ->
                        if (bookSearchItems.isNotEmpty()) {
                            SearchState.SuccessState(bookSearchItems)
                        } else {
                            SearchState.EmptyState
                        }
                    }
                    .doOnError { throwable ->
                        searchState.postValue(SearchState.ErrorState(throwable))
                    }
                    .subscribe(searchState::postValue, Timber::e)
                    .addTo(compositeDisposable)
        }
    }

    private fun resolveSource(query: String, keepLocal: Boolean): Observable<List<BookSearchItem>> {
        return if (keepLocal) localSearch(query) else onlineSearch(query)
    }

    private fun localSearch(query: String): Observable<List<BookSearchItem>> {
        return bookRepository.search(query)
                .map { it.map(bookTransform) }
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
                                    .mapTo(list, transform = bookTransform)
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