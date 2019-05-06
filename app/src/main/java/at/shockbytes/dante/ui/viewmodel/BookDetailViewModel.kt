package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.Parcelable
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    13.06.2018
 */
class BookDetailViewModel @Inject constructor(
    private val bookDao: BookEntityDao,
    private val tracker: Tracker,
    private val settings: DanteSettings
) : BaseViewModel() {

    data class DetailViewState(
        val book: BookEntity,
        val showSummary: Boolean
    )

    private val viewState = MutableLiveData<DetailViewState>()
    fun getViewState(): LiveData<DetailViewState> = viewState

    private val showBookFinishedDialogSubject = PublishSubject.create<String>()
    val showBookFinishedDialogEvent: Observable<String> = showBookFinishedDialogSubject

    private val showPagesDialogSubject = PublishSubject.create<PageInfo>()
    val showPagesDialogEvent: Observable<PageInfo> = showPagesDialogSubject

    private val showNotesDialogSubject = PublishSubject.create<NotesInfo>()
    val showNotesDialogEvent: Observable<NotesInfo> = showNotesDialogSubject

    private val showRatingDialogSubject = PublishSubject.create<RatingInfo>()
    val showRatingDialogEvent: Observable<RatingInfo> = showRatingDialogSubject

    fun initializeWithBookId(id: Long) {
        fetchBook(id)
    }

    private fun fetchBook(bookId: Long) {
        bookDao.get(bookId)?.let { entity ->
            viewState.postValue(craftViewSate(entity))
        }
    }

    fun requestNotesDialog() {
        val instance = getBookFromLiveData() ?: return
        showNotesDialogSubject.onNext(NotesInfo(instance.title, instance.thumbnailAddress, instance.notes
                ?: ""))
    }

    fun requestRatingDialog() {
        val instance = getBookFromLiveData() ?: return
        showRatingDialogSubject.onNext(RatingInfo(instance.title, instance.thumbnailAddress, instance.rating))
    }

    fun requestPageDialog() {
        val instance = getBookFromLiveData() ?: return
        showPagesDialogSubject.onNext(PageInfo(instance.currentPage, instance.pageCount, instance.reading))
    }

    // ------------------------------------------------------------

    fun moveBookToDone() {
        val copy = getBookFromLiveData()?.copy() ?: return
        copy.updateState(BookState.READ)
        updateDaoAndObserver(copy)
    }

    fun updateBookPages(current: Int, pages: Int) {
        val copy = getBookFromLiveData()?.copy(currentPage = current, pageCount = pages) ?: return
        updateDaoAndObserver(copy)
    }

    fun updatePublishedDate(publishedDate: String) {
        val copy = getBookFromLiveData()?.copy(publishedDate = publishedDate) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateCurrentPage(currentPage: Int) {
        val copy = getBookFromLiveData()?.copy(currentPage = currentPage) ?: return
        updateDaoAndObserver(copy)

        if (copy.currentPage == copy.pageCount) {
            showBookFinishedDialogSubject.onNext(copy.title)
        }
    }

    fun updateNotes(notes: String) {
        val copy = getBookFromLiveData()?.copy(notes = notes) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateRating(rating: Int) {
        tracker.trackEvent(DanteTrackingEvent.RatingEvent(rating))

        val copy = getBookFromLiveData()?.copy(rating = rating) ?: return
        updateDaoAndObserver(copy)
    }

    fun updateWishlistDate(wishlistDate: Long): Boolean {

        val book = getBookFromLiveData() ?: return false
        val start = book.startDate
        val end = book.endDate

        return if (checkDateBoundaries(wishlistDate, start, end)) {
            val copy = book.copy(wishlistDate = wishlistDate)
            updateDaoAndObserver(copy)
            true
        } else false
    }

    fun updateStartDate(startDate: Long): Boolean {

        val book = getBookFromLiveData() ?: return false
        val wishlist = book.wishlistDate
        val end = book.endDate

        return if (checkDateBoundaries(wishlist, startDate, end)) {
            val copy = book.copy(startDate = startDate)
            updateDaoAndObserver(copy)
            true
        } else false
    }

    fun updateEndDate(endDate: Long): Boolean {

        val book = getBookFromLiveData() ?: return false
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
        if ((wishlist <= start || start == 0L) && (wishlist <= end || end == 0L)) {
            return true
        }

        // Start specific cases
        if ((start >= wishlist || wishlist == 0L) && (start <= end || end == 0L)) {
            return true
        }

        // End specific cases
        if ((end <= start || start == 0L) && (end <= wishlist || wishlist == 0L)) {
            return false
        }

        return false
    }

    private fun getBookFromLiveData(): BookEntity? {
        return viewState.value?.book
    }

    private fun craftViewSate(book: BookEntity): DetailViewState {
        return DetailViewState(book, settings.showSummary)
    }

    private fun updateDaoAndObserver(b: BookEntity) {
        bookDao.update(b)
        viewState.postValue(craftViewSate(b))
    }

    data class PageInfo(
        val currentPage: Int,
        val pageCount: Int,
        val isReading: Boolean
    )

    data class NotesInfo(
        val title: String,
        val thumbnailUrl: String?,
        val notes: String
    )

    @Parcelize
    data class RatingInfo(
        val title: String,
        val thumbnailUrl: String?,
        val rating: Int
    ) : Parcelable
}