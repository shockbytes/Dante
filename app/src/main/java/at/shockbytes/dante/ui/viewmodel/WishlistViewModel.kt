package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.sort.SortComparators
import javax.inject.Inject

class WishlistViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val settings: DanteSettings
) : BaseViewModel() {

    sealed class WishlistState {

        object Empty : WishlistState()

        data class Present(val books: List<BookEntity>): WishlistState()
    }

    private val wishlist = MutableLiveData<WishlistState>()
    fun getWishlist(): LiveData<WishlistState> = wishlist

    private val sortComparator: Comparator<BookEntity>
        get() = SortComparators.of(settings.sortStrategy)

    fun requestWishlist() {
        bookRepository.bookObservable
            .map { fetchedBooks ->
                fetchedBooks
                    .filter { it.state == BookState.WISHLIST }
                    .sortedWith(sortComparator)
            }
            .map(::mapBooksToWishlistState)
            .doOnError {
                wishlist.postValue(WishlistState.Empty)
            }
            .subscribe(wishlist::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun mapBooksToWishlistState(books: List<BookEntity>): WishlistState {
        return if (books.isEmpty()) {
            WishlistState.Empty
        } else {
            WishlistState.Present(books)
        }
    }
}