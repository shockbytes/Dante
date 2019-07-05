package at.shockbytes.dante.network.google

import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Observable

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class GoogleBooksDownloader(
    private val api: GoogleBooksApi,
    private val schedulerFacade: SchedulerFacade
) : BookDownloader {

    override fun downloadBook(isbn: String): Observable<BookSuggestion> {
        return api
            .downloadBookSuggestion(isbn)
            .observeOn(schedulerFacade.ui)
            .subscribeOn(schedulerFacade.io)
    }
}
