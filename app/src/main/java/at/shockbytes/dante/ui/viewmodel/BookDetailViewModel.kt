package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.SingleLiveEvent
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

    val showBookFinishedDialog = SingleLiveEvent<String>()
    val showPagesDialog = SingleLiveEvent<Triple<Int, Int, Boolean>>()
    val showNotesDialog = SingleLiveEvent<Triple<String, String?, String>>()
    val showRatingDialog = SingleLiveEvent<Triple<String, String?, Int>>()

    init {
        poke()
    }

    override fun poke() {
        // Do nothing...
    }

    private fun fetchBook() {
        bookDao.get(bookId)?.let { book.postValue(it) }
    }


    fun requestNotesDialog() {
        val instance = book.value ?: return
        showNotesDialog.postValue(Triple(instance.title, instance.thumbnailAddress, instance.notes
                ?: ""))
    }

    fun requestRatingDialog() {
        val instance = book.value ?: return
        showRatingDialog.postValue(Triple(instance.title, instance.thumbnailAddress, instance.rating))
    }

    fun requestPageDialog() {
        val instance = book.value ?: return
        showPagesDialog.postValue(Triple(instance.currentPage, instance.pageCount, instance.reading))
    }

    // ------------------------------------------------------------

    fun moveBookToDone() {
        val copy = book.value?.copy() ?: return
        copy.updateState(BookState.READ)
        updateDaoAndObserver(copy)
    }

    fun updateBookPages(current: Int, pages: Int) {
        val copy = book.value?.copy(currentPage = current, pageCount = pages) ?: return
        updateDaoAndObserver(copy)
    }

    fun updatePublishedDate(publishedDate: String) {
        val copy = book.value?.copy(publishedDate = publishedDate) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateCurrentPage(currentPage: Int) {
        val copy = book.value?.copy(currentPage = currentPage) ?: return
        updateDaoAndObserver(copy)

        if (copy.currentPage == copy.pageCount) {
            showBookFinishedDialog.postValue(copy.title)
        }
    }

    fun updateNotes(notes: String) {
        val copy = book.value?.copy(notes = notes) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateRating(rating: Int) {
        val copy = book.value?.copy(rating = rating) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateWishlistDate(wishlistDate: Long): Boolean {

        val book = book.value ?: return false
        val start = book.startDate
        val end = book.endDate

        return if (checkDateBoundaries(wishlistDate, start, end)) {
            val copy = book.copy(wishlistDate = wishlistDate)
            updateDaoAndObserver(copy)
            true
        } else false
    }

    fun updateStartDate(startDate: Long): Boolean {

        val book = book.value ?: return false
        val wishlist = book.wishlistDate
        val end = book.endDate

        return if (checkDateBoundaries(wishlist, startDate, end)) {
            val copy = book.copy(startDate = startDate)
            updateDaoAndObserver(copy)
            true
        } else false
    }

    fun updateEndDate(endDate: Long): Boolean {

        val book = book.value ?: return false
        val wishlist = book.wishlistDate
        val start = book.startDate

        return if (checkDateBoundaries(wishlist, start, endDate)) {
            val copy = book.copy(endDate = endDate)
            updateDaoAndObserver(copy)
            true
        } else false
    }

    private fun checkDateBoundaries(wishlist: Long, start: Long, end: Long): Boolean {

        // Wishlist specific cases
        if ((wishlist < start || start == 0L) && (wishlist < end || end == 0L)) {
            return true
        }

        // Start specific cases
        if((start > wishlist || wishlist == 0L) && (start < end || end == 0L)) {
            return true
        }

        // End specific cases
        if ((end < start || start == 0L) && (end < wishlist || wishlist == 0L)) {
            return false
        }

        return false
    }

    private fun updateDaoAndObserver(b: BookEntity) {
        bookDao.update(b)
        book.postValue(b)
    }

}