package at.shockbytes.dante.books

import android.annotation.SuppressLint
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import kotlinx.android.parcel.Parcelize

/**
 * @author Martin Macheiner
 * Date: 03.02.2018.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class BookSearchSuggestion(val bookId: Long, val title: String,
                                val author: String, val thumbnailAddress: String?,
                                val isbn: String) : SearchSuggestion {

    override fun getBody() = title

}