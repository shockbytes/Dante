package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import android.net.Uri
import androidx.lifecycle.LiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.image.ImagePicker
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

    private val thumbnailUrl = MutableLiveData<Uri>()
    fun getThumbnailUrl(): LiveData<Uri> = thumbnailUrl

    private val addEvent = PublishSubject.create<AddEvent>()
    val onAddEvent: Observable<AddEvent> = addEvent

    /**
     * Call reset at #onCreate() in order to avoid a already set thumbnailAddress from the previous
     * ViewModel usage
     */
    fun reset() {
        thumbnailUrl.value = null
    }

    fun pickImage(activity: androidx.fragment.app.FragmentActivity) {
        imagePicker.openGallery(activity).subscribe({ uri ->
            thumbnailUrl.postValue(uri)
        }, { throwable ->
            Timber.e(throwable)
        }).addTo(compositeDisposable)
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
            addEvent.onNext(AddEvent.SuccessEvent)
        } else {
            addEvent.onNext(AddEvent.ErrorEvent)
        }
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

    fun withBook(bookEntity: BookEntity) {
        // TODO
        Timber.d("With book entity $bookEntity")
    }

    sealed class AddEvent {

        object SuccessEvent : AddEvent()
        object ErrorEvent : AddEvent()
    }
}