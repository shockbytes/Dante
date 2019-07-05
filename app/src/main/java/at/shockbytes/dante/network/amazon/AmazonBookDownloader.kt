package at.shockbytes.dante.network.amazon

import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Observable

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class AmazonBookDownloader(
    private val api: AmazonItemLookupApi,
    private val schedulerFacade: SchedulerFacade
) : BookDownloader {

    private val accessKey = "TODO"
    private val associateTag = "TODO"

    override fun downloadBook(isbn: String): Observable<BookSuggestion> {
        return api
            .downloadBookSuggestion(
                "AWSECommerceService",
                "ItemLookup",
                "Large",
                "All",
                "ISBN",
                isbn,
                accessKey,
                associateTag,
                createTimestamp(),
                createSignature()
            )
            .observeOn(schedulerFacade.ui)
            .subscribeOn(schedulerFacade.io)
    }

    private fun createTimestamp(): String {
        return ""
    }

    private fun createSignature(): String {
        return ""
    }
}
