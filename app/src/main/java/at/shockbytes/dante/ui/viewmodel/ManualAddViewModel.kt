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
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddViewModel @Inject constructor(
    private val bookDao: BookEntityDao,
    private val imagePicker: ImagePicker
) : BaseViewModel() {

    data class BookUpdateData(
        val title: String?,
        val author: String?,
        val pageCount: Int?,
        val subTitle: String?,
        val publishedDate: String?,
        val isbn: String?,
        val language: String?,
        val summary: String?
    ) {
        val isValid: Boolean
            get() = title != null && author != null && pageCount != null
    }

    sealed class AddEvent {
        object Success : AddEvent()
        object Updated : AddEvent()
        object Error : AddEvent()
    }

    sealed class ViewState {
        object ManualAdd : ViewState()
        data class UpdateBook(val bookEntity: BookEntity) : ViewState()
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
        bookUpdateData: BookUpdateData,
        state: BookState
    ) {
        val entity = createEntity(
            bookUpdateData.title,
            bookUpdateData.author,
            bookUpdateData.pageCount,
            state,
            bookUpdateData.subTitle,
            bookUpdateData.publishedDate,
            bookUpdateData.isbn,
            thumbnailUrl.value?.toString(),
            bookUpdateData.language,
            bookUpdateData.summary
        )

        if (entity != null) {
            bookDao.create(entity)
            addEvent.onNext(AddEvent.Success)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    fun updateBook(bookUpdateData: BookUpdateData) {

        if (bookUpdateData.isValid) {

            viewState.value?.let { v ->
                if (v is ViewState.UpdateBook) {
                    val updatedEntity = v.bookEntity.updateFromBookUpdateData(bookUpdateData)
                    bookDao.update(updatedEntity)
                    addEvent.onNext(AddEvent.Updated)
                } else {
                    addEvent.onNext(AddEvent.Error)
                }
            } ?: addEvent.onNext(AddEvent.Error)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    private fun BookEntity.updateFromBookUpdateData(bookUpdateData: BookUpdateData): BookEntity {
        return this.copy(
            title = bookUpdateData.title ?: title,
            author = bookUpdateData.author ?: author,
            pageCount = bookUpdateData.pageCount ?: pageCount,
            subTitle = bookUpdateData.subTitle ?: subTitle,
            publishedDate = bookUpdateData.publishedDate ?: publishedDate,
            isbn = bookUpdateData.isbn ?: isbn,
            language = bookUpdateData.language ?: language,
            summary = bookUpdateData.summary ?: summary
        )
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