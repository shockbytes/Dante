package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    13.06.2018
 */
class BookDetailViewModel @Inject constructor(private val bookDao: BookEntityDao) : BaseViewModel() {

    var bookId: Long = -1
        set(value) {
            field = value
            fetchBook()
        }

    val book = MutableLiveData<BookEntity>()

    init {
        poke()
    }

    override fun poke() {
        // TODO
    }

    private fun fetchBook() {
        bookDao.get(bookId)?.let { book.postValue(it) }
    }

}