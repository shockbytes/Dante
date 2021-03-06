package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import javax.inject.Inject

class LabelCategoryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val books = MutableLiveData<List<BookEntity>>()
    fun getBooks(): LiveData<List<BookEntity>> = books

    fun requestBooksWithLabel(label: BookLabel) {
        bookRepository.bookObservable
            .map { books ->
                books.filter { book ->
                    book.labels.any { it.title == label.title }
                }
            }
            .subscribe(books::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }
}
