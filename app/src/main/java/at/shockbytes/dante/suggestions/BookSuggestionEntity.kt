package at.shockbytes.dante.suggestions

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState

data class BookSuggestionEntity(
    val title: String = "",
    val subTitle: String = "",
    val author: String = "",
    val state: BookState = BookState.WISHLIST,
    val pageCount: Int = 0,
    val publishedDate: String = "",
    val isbn: String = "",
    val thumbnailAddress: String? = null,
    val googleBooksLink: String? = null,
    val language: String? = "NA",
    val summary: String? = null
) {

    companion object {

        fun ofBookEntity(book: BookEntity): BookSuggestionEntity {
            return BookSuggestionEntity(
                title = book.title,
                subTitle = book.subTitle,
                author = book.author,
                pageCount = book.pageCount,
                publishedDate = book.publishedDate,
                isbn = book.isbn,
                thumbnailAddress = book.thumbnailAddress,
                googleBooksLink = book.googleBooksLink,
                language = book.language,
                summary = book.summary
            )
        }
    }
}
