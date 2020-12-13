package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.signin.SignInRepository
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.SuggestionRequest
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.suggestions.cache.SuggestionsCache
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FirebaseSuggestionsRepository(
    private val firebaseSuggestionsApi: FirebaseSuggestionsApi,
    private val schedulers: SchedulerFacade,
    private val signInRepository: SignInRepository,
    private val suggestionsCache: SuggestionsCache
) : SuggestionsRepository {

    override fun loadSuggestions(accessTimestamp: Long, scope: CoroutineScope): Single<Suggestions> {
        return if (shouldUseRemoteData(accessTimestamp)) {
            loadRemoteSuggestions(scope)
        } else {
            loadCachedSuggestions(scope)
        }
    }

    private fun shouldUseRemoteData(accessTimestamp: Long): Boolean {
        // TODO Locally store timestamp, return cached value for one week
        // TODO Or use a fixed timestamp, e.g.: next friday at 8:00 AM
        return true
    }

    private fun loadRemoteSuggestions(scope: CoroutineScope): Single<Suggestions> {
        return signInRepository.getAuthorizationHeader()
            .flatMap(firebaseSuggestionsApi::getSuggestions)
            .doOnSuccess { suggestions ->
                cacheRemoteSuggestions(suggestions, scope)
            }
            .subscribeOn(schedulers.io)
    }

    private fun cacheRemoteSuggestions(suggestions: Suggestions, scope: CoroutineScope) {
        scope.launch {
            suggestionsCache.cache(suggestions)
        }
    }

    private fun loadCachedSuggestions(scope: CoroutineScope): Single<Suggestions> {
        return suggestionsCache.loadSuggestions()
    }

    override fun reportSuggestion(suggestionId: String): Completable {
        return signInRepository.getAuthorizationHeader()
            .flatMapCompletable { bearerToken ->
                firebaseSuggestionsApi.reportSuggestion(bearerToken, suggestionId)
            }
            .subscribeOn(schedulers.io)
    }

    override fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable {
        return signInRepository.getAuthorizationHeader()
            .flatMapCompletable { bearerToken ->
                firebaseSuggestionsApi.suggestBook(
                    bearerToken,
                    SuggestionRequest(
                        BookSuggestionEntity.ofBookEntity(bookEntity),
                        recommendation
                    )
                )
            }
            .subscribeOn(schedulers.io)
    }
}