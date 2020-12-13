package at.shockbytes.dante.suggestions.cache

import at.shockbytes.dante.suggestions.Suggestions
import io.reactivex.Single

interface SuggestionsCache {

    suspend fun cache(suggestions: Suggestions)

    fun loadSuggestions(): Single<Suggestions>
}