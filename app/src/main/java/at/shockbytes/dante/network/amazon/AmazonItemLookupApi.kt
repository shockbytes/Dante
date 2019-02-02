package at.shockbytes.dante.network.amazon

import at.shockbytes.dante.book.BookSuggestion
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Author: Martin Macheiner
 * Date: 13.02.2017
 */
interface AmazonItemLookupApi {

    @GET("json")
    fun downloadBookSuggestion(
        @Query("Service") service: String,
        @Query("Operation") operation: String,
        @Query("ResponseGroup") respGroup: String,
        @Query("SearchIndex") searchIndex: String,
        @Query("IdType") idType: String,
        @Query("ItemId") isbn: String,
        @Query("AWSAccessKeyId") accessKey: String,
        @Query("AssociateTag") associateTag: String,
        @Query("Timestamp") timestamp: String,
        @Query("Signature") requestSignature: String
    ): Observable<BookSuggestion>

    companion object {
        const val SERVICE_ENDPOINT = "http://webservices.amazon.com/onca/"
    }
}
