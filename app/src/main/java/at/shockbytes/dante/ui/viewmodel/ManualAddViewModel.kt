package at.shockbytes.dante.ui.viewmodel


import android.app.FragmentManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.SingleLiveEvent
import at.shockbytes.dante.util.addTo
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddViewModel @Inject constructor(private val bookDao: BookEntityDao) : BaseViewModel() {

    val imageEvent = MutableLiveData<Uri>()

    private var thumbnailAddress: Uri? = null

    val addEvent = PublishSubject.create<AddEvent>()

    init {
        poke()
    }

    override fun poke() {
        // Do nothing...
    }

    /**
     * Call reset at #onCreate() in order to avoid a already set thumbnailAddress from the previous
     * ViewModel usage
     */
    fun reset() {
        thumbnailAddress = null
    }

    fun pickImage(c: FragmentManager) {

        RxImagePicker.with(c).requestImage(Sources.GALLERY).subscribe { uri ->
            thumbnailAddress = uri
            imageEvent.postValue(uri)
        }.addTo(compositeDisposable)
    }

    fun storeBook(title: String?, author: String?, pageCount: Int?, state: BookState,
                  subTitle: String?, publishedDate: String?, isbn: String?, language: String?) {

        val entity = createEntity(title, author, pageCount, state, subTitle,
                publishedDate, isbn, thumbnailAddress?.toString(), language)

        if (entity != null) {
            bookDao.create(entity)
            addEvent.onNext(AddEvent.SuccessEvent)
        } else {
            addEvent.onNext(AddEvent.ErrorEvent)
        }
    }

    private fun createEntity(title: String?, author: String?, pageCount: Int?, state: BookState,
                             subTitle: String?, publishedDate: String?, isbn: String?,
                             thumbnailAddress: String?, language: String?): BookEntity? {

        // Check if author, title and pages are null, these values must always be set
        return if (title == null || author == null || pageCount == null) {
            null
        } else {

            val entity = BookEntity(title = title, author = author, pageCount = pageCount,
                    state = state, subTitle = subTitle ?: "", publishedDate = publishedDate ?: "",
                    isbn = isbn ?: "", thumbnailAddress = thumbnailAddress, language = language)

            when (state) {
                BookState.READ_LATER -> entity.wishlistDate = System.currentTimeMillis()
                BookState.READING -> entity.startDate = System.currentTimeMillis()
                BookState.READ -> entity.endDate = System.currentTimeMillis()
            }
            entity
        }
    }

    sealed class AddEvent {

        object SuccessEvent : AddEvent()
        object ErrorEvent : AddEvent()
    }

}