package at.shockbytes.dante.book

/**
 * Author: Martin Macheiner
 * Date: 11.09.2017
 */
class BookSuggestion(val mainSuggestion: BookEntity?, val otherSuggestions: List<BookEntity>) {

    val hasSuggestions: Boolean
        get() = mainSuggestion != null
}
