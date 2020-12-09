package at.shockbytes.dante.suggestions

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

interface SuggestionsRepository {

    fun loadSuggestions(): Single<Suggestions>

    fun reportSuggestion(suggestionId: String): Completable

    fun suggestBook(bookEntity: BookEntity): Completable
}