package at.shockbytes.dante.books

import at.shockbytes.dante.util.books.Book

/**
 * @author Martin Macheiner
 * Date: 11.09.2017.
 */

class BookSuggestion(val mainSuggestion: Book?, val otherSuggestions: List<Book>) {

    val hasSuggestions: Boolean
        get () = mainSuggestion != null

}
