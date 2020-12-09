package at.shockbytes.dante.suggestions

import android.content.Context
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.util.Assets
import com.google.gson.Gson
import io.reactivex.Completable
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

    override fun reportSuggestion(suggestionId: String): Completable {
        throw UnsupportedOperationException("Unable to report a suggestion in ${javaClass.simpleName}")
    }

    override fun suggestBook(bookEntity: BookEntity): Completable {
        throw UnsupportedOperationException("Unable to suggest a book in ${javaClass.simpleName}")
    }
}