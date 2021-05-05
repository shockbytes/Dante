package at.shockbytes.dante.suggestions.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.preferencesSetKey
import androidx.datastore.preferences.createDataStore
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.util.fromJson
import at.shockbytes.dante.util.singleOf
import com.google.gson.Gson
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DataStoreSuggestionsCache(
    context: Context,
    private val gson: Gson
) : SuggestionsCache {

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "suggestions_cache"
    )

    private val suggestionsKey = preferencesKey<String>(KEY_SUGGESTION_CACHE)
    private val cacheKey = preferencesKey<Long>(KEY_CACHE)
    private val reportedSuggestionsKey = preferencesSetKey<String>(KEY_REPORTED_SUGGESTION_CACHE)
    private val likesSuggestionsKey = preferencesSetKey<String>(KEY_LIKED_SUGGESTION_CACHE)

    override fun lastCacheTimestamp(): Single<Long> {
        return singleOf {
            runBlocking {
                dataStore.data.first()[cacheKey] ?: -1
            }
        }
    }

    override suspend fun cache(suggestions: Suggestions) {
        dataStore.edit { preferences ->
            preferences[suggestionsKey] = gson.toJson(suggestions)
            preferences[cacheKey] = System.currentTimeMillis()
        }
    }

    override fun loadSuggestions(): Single<Suggestions> {
        return Single
            .zip(
                loadReportedSuggestions(),
                loadLikedSuggestions(),
                { reportedSuggestionIds, likedSuggestionIds ->

                    loadSuggestionsFromCache().suggestions
                        .map { suggestion ->

                            val isLikedByMe =
                                likedSuggestionIds.contains(suggestion.suggestionId)
                            val isReportedByMe =
                                reportedSuggestionIds.contains(suggestion.suggestionId)

                            suggestion.copy(
                                isLikedByMe = isLikedByMe,
                                isReportedByMe = isReportedByMe,
                                likes = suggestion.likes.inc()
                            )
                        }
                        .let(::Suggestions)
                }
            )
    }

    private fun loadSuggestionsFromCache(): Suggestions {
        return runBlocking {
            dataStore.data.first()[suggestionsKey]
        }?.let { data ->
            gson.fromJson<Suggestions>(data)
        } ?: Suggestions(listOf())
    }

    override suspend fun cacheSuggestionReport(suggestionId: String) {
        dataStore.edit { preferences ->
            val reports = preferences[reportedSuggestionsKey].orEmpty().toMutableSet()
            reports.add(suggestionId)
            preferences[reportedSuggestionsKey] = reports
        }
    }

    override fun loadReportedSuggestions(): Single<List<String>> {
        return singleOf {
            runBlocking {
                dataStore.data.first()[reportedSuggestionsKey].orEmpty().toList()
            }
        }
    }

    override suspend fun cacheSuggestionLike(suggestionId: String) {
        dataStore.edit { preferences ->
            val likes = preferences[likesSuggestionsKey].orEmpty().toMutableSet()
            likes.add(suggestionId)
            preferences[likesSuggestionsKey] = likes
        }
    }

    override suspend fun removeSuggestionLike(suggestionId: String) {
        dataStore.edit { preferences ->
            val likes = preferences[likesSuggestionsKey].orEmpty().toMutableSet()
            if (likes.remove(suggestionId)) {
                preferences[likesSuggestionsKey] = likes
            }
        }
    }

    override fun loadLikedSuggestions(): Single<List<String>> {
        return singleOf {
            runBlocking {
                dataStore.data.first()[likesSuggestionsKey].orEmpty().toList()
            }
        }
    }

    companion object {
        private const val KEY_SUGGESTION_CACHE = "key_suggestion_cache"
        private const val KEY_CACHE = "key_cache"
        private const val KEY_REPORTED_SUGGESTION_CACHE = "key_reported_suggestion_cache"
        private const val KEY_LIKED_SUGGESTION_CACHE = "key_liked_suggestion_cache"
    }
}