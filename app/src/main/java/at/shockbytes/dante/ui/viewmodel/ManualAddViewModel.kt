package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.image.ImagePicker
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel.ImageState.ThumbnailUri
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
    private val bookRepository: BookRepository,
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
        val summary: String?,
        val thumbnailAddress: String?
    ) {
        val isValid: Boolean
            get() = title != null && author != null && pageCount != null
    }

    sealed class AddEvent {
        object Success : AddEvent()
        data class Updated(val updateBookState: BookState) : AddEvent()
        object Error : AddEvent()
    }

    sealed class ViewState {
        object ManualAdd : ViewState()
        data class UpdateBook(val bookEntity: BookEntity) : ViewState()
    }

    sealed class ImageState {

        data class ThumbnailUri(val uri: Uri) : ImageState()
        object NoImage : ImageState()
    }

    private val imageState = MutableLiveData<ImageState>()
    fun getImageState(): LiveData<ImageState> = imageState

    private val addEvent = PublishSubject.create<AddEvent>()
    val onAddEvent: Observable<AddEvent> = addEvent

    private val viewState = MutableLiveData<ViewState>()
    fun getViewState(): LiveData<ViewState> = viewState

    fun initialize(bookEntity: BookEntity?) {
        if (bookEntity != null) {
            viewState.postValue(ViewState.UpdateBook(bookEntity))

            val image = bookEntity.normalizedThumbnailUrl
                ?.let { ThumbnailUri(it.toUri()) }
                ?: ImageState.NoImage
            imageState.postValue(image)
        } else {
            viewState.postValue(ViewState.ManualAdd)
            imageState.postValue(ImageState.NoImage)
        }
    }

    fun pickImage(activity: FragmentActivity) {
        imagePicker
            .openGallery(activity)
            .map(::ThumbnailUri)
            .doOnNext { thumbnailUri ->
                Timber.d("Image thumbnail picked: ${thumbnailUri.uri}")
            }
            .subscribe(imageState::postValue, ExceptionHandlers::defaultExceptionHandler)
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
            getImageUri(),
            bookUpdateData.language,
            bookUpdateData.summary
        )

        if (entity != null) {
            // TODO REACTIVE Subscribe to this
            bookRepository.create(entity)
            addEvent.onNext(AddEvent.Success)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    fun getImageUri(): String? {
        return imageState.value?.let { state ->
            when (state) {
                is ThumbnailUri -> state.uri.toString()
                ImageState.NoImage -> null
            }
        }
    }

    fun updateBook(bookUpdateData: BookUpdateData) {

        if (bookUpdateData.isValid) {

            viewState.value?.let { v ->
                if (v is ViewState.UpdateBook) {
                    val updatedEntity = v.bookEntity.updateFromBookUpdateData(bookUpdateData)
                    bookRepository.update(updatedEntity)
                    addEvent.onNext(AddEvent.Updated(updatedEntity.state))
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
            summary = bookUpdateData.summary ?: summary,
            thumbnailAddress = bookUpdateData.thumbnailAddress
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