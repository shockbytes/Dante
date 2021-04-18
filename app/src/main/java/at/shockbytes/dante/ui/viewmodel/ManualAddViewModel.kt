package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.image.picker.ImagePicking
import at.shockbytes.dante.storage.ImageUploadStorage
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel.ImageState.ThumbnailUri
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val imagePicker: ImagePicking,
    private val imageUploadStorage: ImageUploadStorage
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
        data class Success(val createdBookState: BookState) : AddEvent()
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

    sealed class ImageLoadingState {
        object Success : ImageLoadingState()
        data class Loading(val progress: Int) : ImageLoadingState()
        data class Error(val throwable: Throwable) : ImageLoadingState()
    }

    private val imageLoadingState = PublishSubject.create<ImageLoadingState>()
    fun getImageLoadingState(): Observable<ImageLoadingState> = imageLoadingState

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
            .flatMap { imageUri ->
                imageUploadStorage.uploadCustomImage(imageUri, ::progressUpdate)
            }
            .map(::ThumbnailUri)
            .doOnSuccess { thumbnailUri ->
                Timber.d("Image thumbnail uploaded and picked picked: ${thumbnailUri.uri}")
                imageLoadingState.onNext(ImageLoadingState.Success)
            }
            .doOnError { throwable ->
                imageLoadingState.onNext(ImageLoadingState.Error(throwable))
            }
            .subscribe(imageState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun progressUpdate(progress: Int) {
        imageLoadingState.onNext(ImageLoadingState.Loading(progress))
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
            storeBookInRepository(entity)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    private fun storeBookInRepository(entity: BookEntity) {
        bookRepository.create(entity)
            .subscribe({
                addEvent.onNext(AddEvent.Success(entity.state))
            }, { throwable ->
                addEvent.onNext(AddEvent.Error)
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
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

            (viewState.value as? ViewState.UpdateBook)
                ?.let { v ->
                    updateBookInRepository(v, bookUpdateData)
                }
                ?: addEvent.onNext(AddEvent.Error)
        } else {
            addEvent.onNext(AddEvent.Error)
        }
    }

    private fun updateBookInRepository(v: ViewState.UpdateBook, bookUpdateData: BookUpdateData) {
        val updatedEntity = v.bookEntity.updateFromBookUpdateData(bookUpdateData)
        bookRepository.update(updatedEntity)
            .subscribe({
                addEvent.onNext(AddEvent.Updated(updatedEntity.state))
            }, {
                addEvent.onNext(AddEvent.Error)
            })
            .addTo(compositeDisposable)
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
                BookState.WISHLIST -> throw IllegalStateException("WISHLIST not supported for manual adding")
            }
            entity
        }
    }
}