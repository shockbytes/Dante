package at.shockbytes.dante.network.amazon

import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.books.BookSuggestion
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class AmazonBookDownloader(private val api: AmazonItemLookupApi) : BookDownloader {

    private val accessKey = "TODO"
    private val associateTag = "TODO"

    override fun downloadBookSuggestion(isbn: String): Observable<BookSuggestion> {
        return api.downloadBookSuggestion("AWSECommerceService", "ItemLookup", "Large",
                "All", "ISBN", isbn, accessKey, associateTag, createTimestamp(),
                createSignature())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    private fun createTimestamp(): String {
        return ""
    }

    private fun createSignature(): String {
        return ""
    }

}
