package at.shockbytes.dante.network

import at.shockbytes.dante.book.BookSuggestion
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

interface BookDownloader {

    fun downloadBook(isbn: String): Observable<BookSuggestion>

    companion object {

        const val MAX_FETCH_AMOUNT = 10
    }
}
