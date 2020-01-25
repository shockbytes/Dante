package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookLabel
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor() : BaseViewModel() {

    private val bookLabels = MutableLiveData<List<BookLabel>>()
    fun getBookLabels(): LiveData<List<BookLabel>> = bookLabels

    fun requestAvailableLabels() {

        bookLabels.postValue(
            listOf(
                BookLabel("Must read", "#FF0000"),
                BookLabel("Science", "#00FF00"),
                BookLabel("Personal Growth", "#0000FF")
            )
        )
    }

}
