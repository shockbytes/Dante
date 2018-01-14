package at.shockbytes.dante.network.google

import at.shockbytes.dante.books.BookSuggestion
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

interface GoogleBooksApi {

    @GET("volumes")
    fun downloadBookSuggestion(@Query("q") query: String): Observable<BookSuggestion>

    companion object {

        val SERVICE_ENDPOINT = "https://www.googleapis.com/books/v1/"
    }

}
