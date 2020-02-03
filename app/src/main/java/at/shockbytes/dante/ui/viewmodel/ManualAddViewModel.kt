package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.net.Uri
import androidx.lifecycle.LiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.image.ImagePicker
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddViewModel @Inject constructor(
    private val bookDao: BookEntityDao,
    private val imagePicker: ImagePicker
) : BaseViewModel() {

    sealed class AddEvent {
        object Success : AddEvent()
        object Error : AddEvent()
    }

    sealed class ViewState {
        object ManualAdd : ViewState()
        data class UpdateBook(val bookEntity: BookEntity): ViewState()
    }

    private val thumbnailUrl = MutableLiveData<Uri>()
    fun getThumbnailUrl(): LiveData<Uri> = thumbnailUrl

    private val addEvent = PublishSubject.create<AddEvent>()
    val onAddEvent: Observable<AddEvent> = addEvent

    private val viewState = MutableLiveData<ViewState>()
    fun getViewState(): LiveData<ViewState> = viewState

    /**
     * Call reset at #onCreate() in order to avoid a already set thumbnailAddress from the previous
     * ViewModel usage
     */
    private fun reset() {
        thumbnailUrl.value = null
    }

    fun initialize(bookEntity: BookEntity?) {
        if (bookEntity != null) {
            viewState.postValue(ViewState.UpdateBook(bookEntity))
            thumbnailUrl.postValue(Uri.parse(bookEntity.normalizedThumbnailUrl))
        } else {
            viewState.postValue(ViewState.ManualAdd)
            reset()
        }
    }

    fun pickImage(activity: androidx.fragment.app.FragmentActivity) {
        imagePicker
            .openGallery(activity)
            .subscribe(thumbnailUrl::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun storeBook(
        title: String?,
        author: String?,
        pageCount: Int?,
        state: BookState,
        subTitle: String?,
        publishedDate: String?,
        isbn: String?,
        language: String?,
        summary: String?
    ) {
        val entity = createEntity(title, author, pageCount, state, subTitle,
                publishedDate, isbn, thumbnailUrl.value?.toString(), language, summary)

        if (entity != null) {
            bookDao.create(entity)
            addEvent.onNext(AddEvent.Success)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    fun updateBook(
        title: String?,
        author: String?,
        pageCount: Int?,
        state: BookState,
        subTitle: String?,
        publishedDate: String?,
        isbn: String?,
        language: String?,
        summary: String?
    ) {
        TODO()
    }

    private fun createEntity(
        title: String?,
        author: String?,
        pageCount: Int?,
        state: BookState,
        subTitle: String?,
        publishedDate: String?,
        isbn: String?,
        thumbnailAddress: String?,
        language: String?,
        summary: String?
    ): BookEntity? {

        // Check if author, title and pages are null, these values must always be set
        return if (title == null || author == null || pageCount == null) {
            null
        } else {

            val entity = BookEntity(
                title = title,
                author = author,
                pageCount = pageCount,
                state = state,
                subTitle = subTitle ?: "",
                publishedDate = publishedDate ?: "",
                isbn = isbn ?: "",
                thumbnailAddress = thumbnailAddress,
                language = language,
                summary = summary
            )

            when (state) {
                BookState.READ_LATER -> entity.wishlistDate = System.currentTimeMillis()
                BookState.READING -> entity.startDate = System.currentTimeMillis()
                BookState.READ -> entity.endDate = System.currentTimeMillis()
            }
            entity
        }
    }
}