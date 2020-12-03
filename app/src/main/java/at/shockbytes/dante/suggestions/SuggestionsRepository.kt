package at.shockbytes.dante.suggestions

import io.reactivex.Single

interface SuggestionsRepository {

    fun loadSuggestions(): Single<Suggestions>
}