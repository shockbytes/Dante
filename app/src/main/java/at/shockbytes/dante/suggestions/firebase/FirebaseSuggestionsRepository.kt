package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.SuggestionRequest
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single

class FirebaseSuggestionsRepository(
    private val firebaseSuggestionsApi: FirebaseSuggestionsApi,
    private val schedulers: SchedulerFacade
) : SuggestionsRepository {

    override fun loadSuggestions(): Single<Suggestions> {
        return firebaseSuggestionsApi.getSuggestions(bearerToken = "")
            .map { firebaseSuggestions ->
                Suggestions(listOf())
            }
            .subscribeOn(schedulers.io)
    }

    override fun reportSuggestion(suggestionId: String): Completable {
        return firebaseSuggestionsApi.reportSuggestion(bearerToken = "", suggestionId)
            .subscribeOn(schedulers.io)
    }

    override fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable {
        val authToken = "" // TODO Receive auth token
        val suggestionRequest = SuggestionRequest(
            BookSuggestionEntity.ofBookEntity(bookEntity),
            recommendation
        )
        return firebaseSuggestionsApi.suggestBook(authToken, suggestionRequest)
            .subscribeOn(schedulers.io)
    }
}