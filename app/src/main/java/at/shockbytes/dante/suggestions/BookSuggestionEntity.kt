package at.shockbytes.dante.suggestions

import at.shockbytes.dante.core.book.BookState

data class BookSuggestionEntity(
    val title: String = "",
    val subTitle: String = "",
    val author: String = "",
    val state: BookState = BookState.READING, // TODO Change to WISHLIST
    val pageCount: Int = 0,
    val publishedDate: String = "",
    val isbn: String = "",
    val thumbnailAddress: String? = null,
    val googleBooksLink: String? = null,
    val language: String? = "NA",
    val summary: String? = null
)
