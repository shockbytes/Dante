package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.suggestions.SuggestionRequest
import at.shockbytes.dante.suggestions.Suggestions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FirebaseSuggestionsApi {

    @GET("suggestions")
    fun getSuggestions(
        @Header("Authorization") bearerToken: String
    ): Single<Suggestions>

    @POST("suggestions/{suggestionId}/report")
    fun reportSuggestion(
        @Header("Authorization") bearerToken: String,
        @Path("suggestionId") suggestionId: String
    ): Completable

    @POST("suggestions")
    fun suggestBook(
        @Header("Authorization") bearerToken: String,
        @Body suggestionRequest: SuggestionRequest
    ): Completable

    companion object {
        const val BASE_URL = "https://us-central1-dante-166506.cloudfunctions.net/app/"
    }
}