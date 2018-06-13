package at.shockbytes.dante.network.google

import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.network.BookDownloader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class GoogleBooksDownloader(private val api: GoogleBooksApi) : BookDownloader {

    override fun downloadBook(isbn: String): Observable<BookSuggestion> {
        return api.downloadBookSuggestion(isbn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }
}
