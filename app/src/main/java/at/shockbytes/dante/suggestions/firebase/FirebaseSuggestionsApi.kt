package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.suggestions.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FirebaseSuggestionsApi {

    @GET("suggestions")
    fun getSuggestions(): Single<FirebaseSuggestions>

    @POST("suggestions/{suggestionId}/report")
    fun reportSuggestion(
        @Query("suggestionId") suggestionId: String
    ): Completable

    @POST("suggestions")
    fun suggestBook(
        @Header("Authorization") bearerToken: String,
        @Body suggestion: Suggestion
    ): Completable

    companion object {

        const val BASE_URL = "https://us-central1-dante-166506.cloudfunctions.net/app"
    }
}