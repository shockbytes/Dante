package at.shockbytes.dante.core.book

sealed class BookLoadingState {

    object Loading : BookLoadingState()

    object Error : BookLoadingState()

    data class Success(val bookSuggestion: BookSuggestion) : BookLoadingState()
}