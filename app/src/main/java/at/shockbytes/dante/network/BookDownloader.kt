package at.shockbytes.dante.network

import at.shockbytes.dante.books.BookSuggestion
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

interface BookDownloader {

    fun downloadBookSuggestion(isbn: String): Observable<BookSuggestion>

}
