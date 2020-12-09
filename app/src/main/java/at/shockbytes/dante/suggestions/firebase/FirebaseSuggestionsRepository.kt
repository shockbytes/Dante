package at.shockbytes.dante.suggestions.firebase

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.signin.SignInRepository
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.SuggestionRequest
import at.shockbytes.dante.suggestions.Suggestions
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single

class FirebaseSuggestionsRepository(
    private val firebaseSuggestionsApi: FirebaseSuggestionsApi,
    private val schedulers: SchedulerFacade,
    private val signInRepository: SignInRepository
) : SuggestionsRepository {

    override fun loadSuggestions(): Single<Suggestions> {
        return signInRepository.getAuthorizationHeader()
            .flatMap(firebaseSuggestionsApi::getSuggestions)
            .map { firebaseSuggestions ->
                // TODO Map suggestions
                Suggestions(listOf())
            }
            .subscribeOn(schedulers.io)
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