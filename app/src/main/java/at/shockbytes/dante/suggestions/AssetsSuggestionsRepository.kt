package at.shockbytes.dante.suggestions

import android.content.Context
import at.shockbytes.dante.util.Assets
import com.google.gson.Gson
import io.reactivex.Single

class AssetsSuggestionsRepository(
    private val context: Context,
    private val gson: Gson
) : SuggestionsRepository {

    override fun loadSuggestions(): Single<Suggestions> {
        return Assets.readFile(context, "suggestions-v1.json")
            .map { jsonContent ->
                gson.fromJson(jsonContent, Suggestions::class.java)
            }
    }
}