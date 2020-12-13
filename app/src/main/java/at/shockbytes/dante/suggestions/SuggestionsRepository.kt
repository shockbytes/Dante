package at.shockbytes.dante.suggestions

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope

interface SuggestionsRepository {

    fun loadSuggestions(
        accessTimestamp: Long = System.currentTimeMillis(),
        scope: CoroutineScope
    ): Single<Suggestions>

    fun reportSuggestion(suggestionId: String): Completable

    fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable
}