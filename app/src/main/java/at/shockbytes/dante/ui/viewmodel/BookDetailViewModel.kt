package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.Parcelable
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.navigation.NotesBundle
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.settings.DanteSettings
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    13.06.2018
 */
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val settings: DanteSettings,
    private val pageRecordDao: PageRecordDao
) : BaseViewModel() {

    data class DetailViewState(
        val book: BookEntity,
        val showSummary: Boolean
    )

    data class PageRecordDataPoint(
        val page: Int,
        val formattedDate: String
    )

    private val viewState = MutableLiveData<DetailViewState>()
    fun getViewState(): LiveData<DetailViewState> = viewState

    private val pageRecords = MutableLiveData<List<PageRecordDataPoint>>()
    fun getPageRecords(): LiveData<List<PageRecordDataPoint>> = pageRecords

    private val showBookFinishedDialogSubject = PublishSubject.create<String>()
    val showBookFinishedDialogEvent: Observable<String> = showBookFinishedDialogSubject

    private val showPagesDialogSubject = PublishSubject.create<PageInfo>()
    val showPagesDialogEvent: Observable<PageInfo> = showPagesDialogSubject

    private val showNotesDialogSubject = PublishSubject.create<NotesBundle>()
    val showNotesDialogEvent: Observable<NotesBundle> = showNotesDialogSubject

    private val showRatingDialogSubject = PublishSubject.create<RatingInfo>()
    val showRatingDialogEvent: Observable<RatingInfo> = showRatingDialogSubject

    private val requestBookEditSubject = PublishSubject.create<BookEntity>()
    val onBookEditRequest: Observable<BookEntity> = requestBookEditSubject

    private val addLabelsSubject = PublishSubject.create<List<BookLabel>>()
    val onAddLabelsRequest: Observable<List<BookLabel>> = addLabelsSubject

    private var bookId: Long = -1L
    private var pagesAtInit: Int? = null

    fun initializeWithBookId(id: Long) {
        this.bookId = id
        fetchBook(bookId)
        fetchPageRecords(bookId)
    }

    private fun fetchBook(bookId: Long) {
        bookRepository.get(bookId)
                ?.also { entity ->
                    pagesAtInit = entity.currentPage
                }
                ?.let(::craftViewState)
                ?.let(viewState::postValue)
    }

    private fun fetchPageRecords(bookId: Long) {
        pageRecordDao.pageRecordsForBook(bookId)
                .map(::mapPageRecordsToDataPoints)
                .subscribe(pageRecords::postValue, ExceptionHandlers::defaultExceptionHandler)
                .addTo(compositeDisposable)
    }

    private fun mapPageRecordsToDataPoints(pageRecords: List<PageRecord>): List<PageRecordDataPoint> {

        val format = DateTimeFormat.forPattern("dd/MM/yy")
        return pageRecords
                .groupBy { record ->
                    DateTime(record.timestamp).withTimeAtStartOfDay()
                }
                .mapNotNull { (dtTimestamp, pageRecords) ->
                    pageRecords.maxBy { it.timestamp }?.let { record ->
                        PageRecordDataPoint(
                                page = record.toPage,
                                formattedDate = format.print(dtTimestamp)
                        )
                    }
                }
    }

    fun requestNotesDialog() {
        val instance = getBookFromLiveData() ?: return

        val notesBundle = NotesBundle(
            instance.title,
            instance.thumbnailAddress,
            instance.notes ?: ""
        )

        showNotesDialogSubject.onNext(notesBundle)
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

        // Wish list specific cases
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

    private fun craftViewState(book: BookEntity): DetailViewState {
        return DetailViewState(book, settings.showSummary)
    }

    private fun updateDaoAndObserver(b: BookEntity) {
        bookRepository.update(b)
        viewState.postValue(craftViewState(b))
    }

    fun requestEditBook() {
        val bookEntity = viewState.value?.book ?: return
        requestBookEditSubject.onNext(bookEntity)
    }

    fun reload() {
        fetchBook(bookId)
    }

    fun attachLabel(bookLabel: BookLabel) {

        val attachableLabel = bookLabel.withBookId(bookId)

        getBookFromLiveData()?.let { book ->

            val updatedLabels = book.labels + attachableLabel
            val copy = book.copy(labels = updatedLabels)
            updateDaoAndObserver(copy)
        }
    }

    fun requestAddLabels() {
        getBookFromLiveData()?.labels?.let(addLabelsSubject::onNext)
    }

    fun removeLabel(label: BookLabel) {
        bookRepository.deleteBookLabel(label)

        // Reload the book once a label got deleted
        fetchBook(bookId)
    }

    fun onPageCountMayChanged() {

        val currentPage = getBookFromLiveData()?.currentPage ?: 0
        val startPage = pagesAtInit ?: 0
        if (currentPage != startPage) {
            pageRecordDao.insertPageRecordForId(
                    id = bookId,
                    fromPage = startPage,
                    toPage = currentPage,
                    nowInMillis = System.currentTimeMillis()
            )
        }
    }

    data class PageInfo(
        val currentPage: Int,
        val pageCount: Int,
        val isReading: Boolean
    )

    @Parcelize
    data class RatingInfo(
        val title: String,
        val thumbnailUrl: String?,
        val rating: Int
    ) : Parcelable
}