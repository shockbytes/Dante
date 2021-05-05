package at.shockbytes.dante.suggestions.cache

import at.shockbytes.dante.suggestions.Suggestions
import io.reactivex.Single

interface SuggestionsCache {

    fun lastCacheTimestamp(): Single<Long>

    suspend fun cache(suggestions: Suggestions)

    fun loadSuggestions(): Single<Suggestions>

    // ------------ Reports ------------

    suspend fun cacheSuggestionReport(suggestionId: String)

    fun loadReportedSuggestions(): Single<List<String>>

    // ------------- Likes -------------

    suspend fun cacheSuggestionLike(suggestionId: String)

    suspend fun removeSuggestionLike(suggestionId: String)

    fun loadLikedSuggestions(): Single<List<String>>
}