package at.shockbytes.dante.suggestions

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope

interface SuggestionsRepository {

    fun loadSuggestions(
        accessTimestamp: Long = System.currentTimeMillis(),
        scope: CoroutineScope
    ): Single<Suggestions>

    fun reportSuggestion(suggestionId: String, scope: CoroutineScope): Completable

    fun getUserReportedSuggestions(): Single<List<String>>

    fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable
}