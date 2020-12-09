package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.suggestions.SuggestionsRepository
import io.reactivex.Completable
import io.reactivex.Single

class FirebaseSuggestionsRepository(
    private val firebaseSuggestionsApi: FirebaseSuggestionsApi
) : SuggestionsRepository {

    override fun loadSuggestions(): Single<Suggestions> {
        TODO("Not yet implemented")
    }

    override fun reportSuggestion(suggestionId: String): Completable {
        TODO("Not yet implemented")
    }

    override fun suggestBook(bookEntity: BookEntity): Completable {
        TODO("Not yet implemented")
    }
}