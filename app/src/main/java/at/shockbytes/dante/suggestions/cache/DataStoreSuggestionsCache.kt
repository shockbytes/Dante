package at.shockbytes.dante.suggestions.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
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

    override suspend fun cache(suggestions: Suggestions) {
        dataStore.edit { preferences ->
            preferences[suggestionsKey] = gson.toJson(suggestions)
        }
    }

    override fun loadSuggestions(): Single<Suggestions> {
        return singleOf {
            runBlocking {
                dataStore.data.first()[suggestionsKey]
            }?.let { data ->
                gson.fromJson<Suggestions>(data)
            } ?: Suggestions(listOf())
        }
    }

    companion object {
        private const val KEY_SUGGESTION_CACHE = "key_suggestion_cache"
    }
}